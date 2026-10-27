package com.lowdragmc.lowdraglib.client.scene;

import java.util.*;
import java.util.function.Function;

/** Cooperative render-thread compilation, with atomic replacement of complete scene geometry. */
public final class NativeSceneGeometryCache<K> {
    private List<K> requests=List.of();
    private List<NativeSceneBlockGeometry> pending=new ArrayList<>();
    private List<NativeSceneBlockGeometry> published=List.of();
    private Function<K,NativeSceneBlockGeometry> extractor;
    private int completed;
    public void rebuild(Collection<K> requests,Function<K,NativeSceneBlockGeometry> extractor) {
        this.requests=List.copyOf(requests);this.extractor=Objects.requireNonNull(extractor);
        pending=new ArrayList<>(this.requests.size());completed=0;
    }
    public void advance(int budget) {
        if(budget<=0)throw new IllegalArgumentException("Scene cache budget must be positive");
        if(extractor==null)return;
        for(int remaining=budget;remaining>0 && completed<requests.size();remaining--) {
            pending.add(Objects.requireNonNull(extractor.apply(requests.get(completed))));
            completed++;
        }
        if(completed==requests.size()) {
            published=List.copyOf(pending);pending=new ArrayList<>();extractor=null;
        }
    }
    public List<NativeSceneBlockGeometry> snapshot(){return published;}
    public boolean compiling(){return extractor!=null;}
    public int completed(){return completed;}
    public int total(){return requests.size();}
    public void clear(){requests=List.of();pending=new ArrayList<>();published=List.of();extractor=null;completed=0;}
}
