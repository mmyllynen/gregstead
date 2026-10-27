"""Pack version ordering, including immutable prerelease versions."""
import re

def version_tuple(version):
    match = re.fullmatch(r'(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?', version)
    if not match:
        raise ValueError('Pack version must be MAJOR.MINOR.PATCH with an optional prerelease')
    prerelease = match[4]
    identifiers = []
    if prerelease:
        for item in prerelease.split('.'):
            if item.isdigit():
                if len(item)>1 and item.startswith('0'):
                    raise ValueError('Numeric prerelease identifiers cannot have leading zeroes')
                identifiers.append((0,int(item)))
            else:
                identifiers.append((1,item))
    return (*map(int,match.groups()[:3]), int(prerelease is None), tuple(identifiers))
