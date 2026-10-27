#!/usr/bin/env python3
"""Generate EMI resource defaults from a real, fully loaded recipe snapshot.

No game files are edited. Costs are monotone across dependencies, so a selected
recipe can never depend on itself, even through tags or alternative producers.
"""
from collections import Counter, defaultdict, deque
from pathlib import Path
import argparse, hashlib, json

ZERO = (0, 0, 0, 0, 0, 0, 0)
POLICY = 'guaranteed / lowest maximum workstation requirement / lowest GT voltage tier / fewest machine, furnace, total steps / depth / vanilla tag alternatives / stable identifier'


def key_value(key):
    return json.loads(key)


def technology(recipe):
    category = recipe['category']
    if category == 'minecraft:crafting':
        return 0
    if category in ('minecraft:smelting', 'minecraft:blasting', 'minecraft:smoking', 'minecraft:campfire_cooking'):
        return 1
    return 2


def voltage_tier(voltage):
    tier, ceiling = 0, 8
    while voltage > ceiling:
        tier += 1
        ceiling *= 4
    return tier


def score(action, costs):
    children = [costs[k] for k in action['deps']]
    tech = technology(action['recipe']) if action['kind'] == 'recipe' else 0
    tier = voltage_tier(action['recipe'].get('voltage', 0)) if tech == 2 else 0
    return (
        sum(x[0] for x in children) + action.get('chanced', 0),
        max([tech, *(x[1] for x in children)]),
        max([tier, *(x[2] for x in children)]),
        sum(x[3] for x in children) + (tech == 2),
        sum(x[4] for x in children) + (tech == 1),
        sum(x[5] for x in children) + 1,
        max([0, *(x[6] for x in children)]) + 1,
    )


def generate(snapshot):
    nodes = dict(snapshot['ingredients'])
    recipes = {r['id']: r for r in sorted(snapshot['recipes'], key=lambda r: r['id'])}
    pinned = {x['key']: x['recipe'] for x in snapshot.get('existingDefaults', [])}
    actions, producers, consumers = [], defaultdict(list), defaultdict(set)
    excluded = Counter()

    def atom(key):
        if key is not None and key not in nodes:
            nodes[key] = {'value': key_value(key), 'stacks': [key], 'tag': False, 'catalyst': False}

    def add(kind, target, deps, **extra):
        i = len(actions)
        action = dict(kind=kind, target=target, deps=tuple(deps), **extra)
        actions.append(action)
        producers[target].append(i)
        for dep in set(deps):
            consumers[dep].add(i)

    for key, node in list(nodes.items()):
        for stack in node.get('stacks', []):
            atom(stack)
        # EMI's static "tags" map can resolve string-serialized tags. Arbitrary
        # list ingredients cannot be encoded in that map: leave them explicit.
        if node.get('tag') and isinstance(node['value'], str):
            for stack in sorted(set(node['stacks'])):
                if stack != key:
                    value = str(nodes[stack]['value'])
                    namespace = 0 if ':minecraft:' in value else (1 if ':gtceu:' in value else 2)
                    common = 0 if 'minecraft:oak_' in value else 1
                    add('tag', key, [stack], choice=stack, tie=f'{namespace}:{common}:' + stack)

    for recipe in recipes.values():
        if not recipe.get('supported', True):
            excluded['unsupported-recipe-tree'] += 1
            continue
        if not recipe['inputs'] or not recipe['outputs']:
            excluded['empty-input-or-output'] += 1
            continue
        members = [i for i in recipe['inputs'] if i.get('chance', 1) > 0]
        if any(i['key'] is None or not nodes[i['key']].get('stacks') for i in members):
            excluded['unserializable-or-empty-input'] += 1
            continue
        deps = [i['key'] for i in members if not i.get('catalyst', nodes[i['key']].get('catalyst', False))]
        for out in recipe['outputs']:
            key = out['key']
            if key is None or out.get('chance', 1) <= 0 or out.get('amount', 1) <= 0:
                continue
            if key in pinned and pinned[key] != recipe['id']:
                continue
            atom(key)
            add('recipe', key, deps, recipe=recipe, tie=recipe['id'], chanced=int(out.get('chance', 1) < 1))

    costs, selected, reasons = {}, {}, {}
    queue, queued = deque(), set()

    def enqueue(index):
        if index not in queued:
            queue.append(index)
            queued.add(index)

    def leaf(key, reason):
        costs[key] = ZERO
        reasons[key] = reason
        for i in sorted(consumers[key]):
            enqueue(i)

    for key in sorted(nodes):
        if not producers[key]:
            reason = 'no-supported-producer'
            if len(nodes[key].get('stacks', [])) > 1 and not nodes[key].get('tag'):
                reason = 'compound-alternatives-not-encodable-in-static-defaults'
            leaf(key, reason)
    for i, action in enumerate(actions):
        if not action['deps']:
            enqueue(i)

    def solve():
        while queue:
            i = queue.popleft()
            queued.remove(i)
            action = actions[i]
            if not all(k in costs for k in action['deps']):
                continue
            value = score(action, costs)
            target = action['target']
            if target not in costs or (value, action['tie']) < (costs[target], selected.get(target, {}).get('tie', '')):
                costs[target] = value
                selected[target] = action
                reasons.pop(target, None)
                for consumer in sorted(consumers[target]):
                    enqueue(consumer)

    solve()
    # Remaining closed components have no terminating manufacturing route. They
    # are base leaves, not invented recipes. Then resolve their acyclic parents.
    unresolved = set(nodes) - set(costs)
    while unresolved:
        before_cut = len(unresolved)
        edges = {k: {d for i in producers[k] for d in actions[i]['deps'] if d in unresolved} for k in unresolved}
        reverse = defaultdict(set)
        for k, deps in edges.items():
            for dep in deps:
                reverse[dep].add(k)
        visited, order = set(), []
        for start in sorted(unresolved):
            if start in visited:
                continue
            stack = [(start, False)]
            while stack:
                key, end = stack.pop()
                if end:
                    order.append(key)
                elif key not in visited:
                    visited.add(key)
                    stack.append((key, True))
                    stack.extend((d, False) for d in sorted(edges[key], reverse=True) if d not in visited)
        components, assigned = [], set()
        for start in reversed(order):
            if start in assigned:
                continue
            component, stack = set(), [start]
            assigned.add(start)
            while stack:
                key = stack.pop()
                component.add(key)
                for dep in reverse[key]:
                    if dep not in assigned:
                        assigned.add(dep)
                        stack.append(dep)
            components.append(component)
        for component in components:
            if not any(edges[k] - component for k in component):
                for key in sorted(component):
                    # Prevent the closed cyclic producers from being reselected.
                    producers[key] = []
                    leaf(key, 'cycle-only-producers')
        blocked = {k for k, reason in reasons.items() if reason == 'cycle-only-producers'}
        for key in blocked:
            for i in consumers[key]:
                enqueue(i)
        # Actions targeting explicit cycle cuts must stay disabled.
        for action in actions:
            if action['target'] in blocked:
                action['deps'] = ('__cycle_cut_never_ready__',)
        solve()
        unresolved = set(nodes) - set(costs)
        if len(unresolved) >= before_cut:
            raise ValueError('Cycle classification made no progress')

    missing = set(nodes) - set(costs)
    if missing:
        raise ValueError('Unclassified dependencies: ' + str(sorted(missing)[:8]))
    # Strictly decreasing cost on every selected edge is the cycle proof.
    for target, action in selected.items():
        if any(costs[dep] >= costs[target] for dep in action['deps']):
            raise ValueError('Non-decreasing selected dependency: ' + target)

    resolutions, tags = defaultdict(list), {}
    for target, action in sorted(selected.items()):
        if action['kind'] == 'recipe' and target not in pinned:
            resolutions[action['recipe']['id']].append(nodes[target]['value'])
        elif action['kind'] == 'tag' and target not in pinned:
            tags[nodes[target]['value']] = nodes[action['choice']]['value']
    defaults = {'resolutions': dict(sorted(resolutions.items())), 'tags': dict(sorted(tags.items()))}
    summary = {
        'policy': POLICY,
        'recipeCount': len(recipes), 'ingredientCount': len(nodes),
        'preservedExistingDefaults': len(pinned),
        'semanticVariantsMerged': sum(k != v for k, v in snapshot.get('semanticAliases', {}).items()),
        'generatedOutputDefaults': sum(map(len, resolutions.values())),
        'generatedTagDefaults': len(tags),
        'leafReasons': dict(sorted(Counter(reasons.values()).items())),
        'excludedRecipes': dict(sorted(excluded.items())),
        'maximumSelectedDepth': max((v[6] for v in costs.values()), default=0),
        'unclassifiedIngredients': 0,
    }
    report = dict(summary, leaves=dict(sorted(reasons.items())), selected={k: {'kind': a['kind'], 'recipe': a.get('recipe', {}).get('id'), 'choice': a.get('choice'), 'deps': a['deps'], 'cost': costs[k]} for k, a in sorted(selected.items())})
    return defaults, report


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('snapshot', type=Path)
    p.add_argument('--output', type=Path, required=True)
    p.add_argument('--report', type=Path, required=True)
    p.add_argument('--summary', type=Path)
    p.add_argument('--inputs', type=Path, help='Optional mod/config SHA-256 manifest for this export')
    args = p.parse_args()
    raw = args.snapshot.read_bytes()
    defaults, report = generate(json.loads(raw))
    if args.inputs:
        report['inputManifest'] = json.loads(args.inputs.read_text())
    report['snapshotSha256'] = hashlib.sha256(raw).hexdigest()
    report['defaultsSha256'] = hashlib.sha256((json.dumps(defaults, indent=2, ensure_ascii=False) + '\n').encode()).hexdigest()
    for path, data in [(args.output, defaults), (args.report, report)]:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n')
    if args.summary:
        summary = {k: v for k, v in report.items() if k not in ('leaves', 'selected')}
        args.summary.write_text(json.dumps(summary, indent=2, ensure_ascii=False) + '\n')
    print(json.dumps({k: v for k, v in report.items() if k not in ('leaves', 'selected')}, indent=2))


if __name__ == '__main__':
    main()
