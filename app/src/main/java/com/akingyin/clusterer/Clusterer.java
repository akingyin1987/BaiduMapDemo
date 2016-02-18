package com.akingyin.clusterer;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;


import com.akingyin.baidumapdemo.Mypoi;
import com.akingyin.baidumapdemo.R;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Projection;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Nacho Lopez on 28/10/13.
 */
public class Clusterer<T extends Clusterable> {

    private static final int NODE_CAPACITY = 60;
    private static final int CLUSTER_CENTER_PADDING = 120;
    private static final QuadTreeBoundingBox WORLD = new QuadTreeBoundingBox(-85, -180, 85, 180);
    public static final int UPDATE_INTERVAL_TIME = 500;
    public static final int CAMERA_ANIMATION_DURATION = 500;
    private static final float MAPS_V2_MAX_ZOOM_LEVEL = 21f;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private BaiduMap googleMap;
    private Context context;
    private QuadTree<T> pointsTree;
    private float oldZoomValue = 0f;
    private LatLngBounds oldBounds;

    private Interpolator animationInterpolator;
    private boolean animationEnabled = false;
    private int animationDuration = 500;
    private MarkerAnimation markerAnimation;

    private OnPaintingClusterListener onPaintingCluster;
    private OnPaintingClusterableMarkerListener onPaintingMarker;
    private OnCameraChangeListener onCameraChangeListener;
    private ReversibleHashMap<T, OverlayOptions> markers;
    private HashMap<OverlayOptions, Cluster<T>> clusterMarkers;
    private UpdateMarkersTask task;
    private final Lock updatingLock;
    private final Handler refreshHandler;
    private float maxZoomScale = MAPS_V2_MAX_ZOOM_LEVEL;
    private MyMarkerManager   manager;

    public Clusterer(Context context, BaiduMap googleMap) {
        this.googleMap = googleMap;
        this.context = context;
        this.googleMap.setOnMapStatusChangeListener(cameraChanged);
        this.googleMap.setOnMarkerClickListener(markerClicked);
        this.markers = new ReversibleHashMap<>();
        this.clusterMarkers = new HashMap<>();
        this.refreshHandler = new Handler();
        this.updatingLock = new ReentrantLock();
        this.animationInterpolator = new LinearInterpolator();
        this.manager = new MyMarkerManager(googleMap);

        initQuadTree();
    }

    private void initQuadTree() {
        this.pointsTree = new QuadTree<>(WORLD, NODE_CAPACITY);
    }

    BaiduMap.OnMapStatusChangeListener cameraChanged = new BaiduMap.OnMapStatusChangeListener() {
        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus) {

        }

        @Override
        public void onMapStatusChange(MapStatus mapStatus) {

        }

        @Override
        public void onMapStatusChangeFinish(MapStatus mapStatus) {

                LatLngBounds mapBounds =  mapStatus.bound;
                if (oldZoomValue != mapStatus.zoom || oldBounds == null || !oldBounds.contains(mapBounds.northeast) || !oldBounds.contains(mapBounds.southwest)) {
                    oldZoomValue = mapStatus.zoom;
                    oldBounds = mapBounds;
                    refreshHandler.removeCallbacks(updateMarkersRunnable);
                    refreshHandler.postDelayed(updateMarkersRunnable, UPDATE_INTERVAL_TIME);

                }
                if (onCameraChangeListener != null) {
                    onCameraChangeListener.onCameraChange(mapStatus);
                }

        }
    };



    private Runnable updateMarkersRunnable = new Runnable() {
        @Override
        public void run() {
            updateMarkers();
        }
    };

    private ClustererClickListener clustererClickListener;

    public ClustererClickListener getClustererClickListener() {
        return clustererClickListener;
    }

    public void setClustererListener(ClustererClickListener<T> clustererClickListener) {
        this.clustererClickListener = clustererClickListener;
    }

    BaiduMap.OnMarkerClickListener markerClicked = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Cluster<T> cluster = clusterMarkers.get(marker);
            if (cluster != null) {
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLngBounds(cluster.getBounds(), CLUSTER_CENTER_PADDING,CLUSTER_CENTER_PADDING);
                googleMap.animateMapStatus(update);
                if (clustererClickListener != null) {
                    clustererClickListener.clusterClicked(cluster);
                }
                return true;
            }
            if (clustererClickListener != null) {

                clustererClickListener.markerClicked(getClusterableFromMarker(marker));
            }
            return false;
        }
    };

    public void forceUpdate() {
        updateMarkers();
    }

    public void add(T marker) {
        pointsTree.insertData(marker);
    }

    public void addAll(List<T> markers) {
        pointsTree.insertData(markers);
    }

    public void clear() {
        initQuadTree();
    }

    public OnPaintingClusterListener getOnPaintingClusterListener() {
        return onPaintingCluster;
    }

    public void setOnPaintingClusterListener(OnPaintingClusterListener onPaintingCluster) {
        this.onPaintingCluster = onPaintingCluster;
    }

    public OnPaintingClusterableMarkerListener getOnPaintingMarkerListener() {
        return onPaintingMarker;
    }

    public void setOnPaintingMarkerListener(OnPaintingClusterableMarkerListener onPaintingMarker) {
        this.onPaintingMarker = onPaintingMarker;
    }

    public OnCameraChangeListener getOnCameraChangeListener() {
        return onCameraChangeListener;
    }

    public void setOnCameraChangeListener(OnCameraChangeListener onCameraChangeListener) {
        this.onCameraChangeListener = onCameraChangeListener;
    }

    public float getMaxZoomScale() {
        return maxZoomScale;
    }

    public void setMaxZoomScale(float maxZoomScale) {
        this.maxZoomScale = maxZoomScale;
    }

    @SuppressWarnings("unchecked")
    protected void updateMarkers() {
        if (task != null) {
            task.cancel(false);
        }
        task = new UpdateMarkersTask(context, googleMap, onPaintingMarker, onPaintingCluster);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(executor, pointsTree);
        } else {
            task.execute(pointsTree);
        }
    }

    private class UpdateMarkersTask extends AsyncTask<QuadTree<T>, Void, ClusteringProcessResultHolder<T>> {

        private WeakReference<BaiduMap> map;
        private LatLngBounds bounds;
        private OnPaintingClusterableMarkerListener onPaintingClusterableMarker;
        private OnPaintingClusterListener onPaintingCluster;
        private Projection projection;
        private int gridInPixels;
        private float zoomScale;
        private boolean performCluster = true;


        UpdateMarkersTask(Context context, BaiduMap map, OnPaintingClusterableMarkerListener onPaintingClusterableMarker,
                          OnPaintingClusterListener onPaintingCluster) {
            this.map = new WeakReference<>(map);
            LatLngBounds originalBounds = map.getMapStatus().bound;

            this.bounds = new LatLngBounds.Builder().include(
                    new LatLng(originalBounds.southwest.latitude - 0.5, originalBounds.southwest.longitude + 0.5))
                  .include(new LatLng(originalBounds.northeast.latitude + 0.5, originalBounds.northeast.longitude - 0.5)).build();
            this.zoomScale = map.getMapStatus().zoom;
            this.performCluster = zoomScale < maxZoomScale;
            this.gridInPixels = (int) (getSizeForZoomScale((int) zoomScale) * context.getResources().getDisplayMetrics().density + 0.5f);
            this.onPaintingCluster = onPaintingCluster;
            this.onPaintingClusterableMarker = onPaintingClusterableMarker;
            this.projection = map.getProjection();
        }

        private int getSizeForZoomScale(int scale) {
            switch (scale) {
                case 13:
                case 14:
                case 15:
                    return 64;
                case 16:
                case 17:
                case 18:
                    return 32;
                case 19:
                    return 16;
                default:
                    return 88;
            }
        }

        private boolean isInDistance(Point origin, Point other) {
            return origin.x >= other.x - gridInPixels && origin.x <= other.x + gridInPixels && origin.y >= other.y - gridInPixels
                    && origin.y <= other.y + gridInPixels;
        }

        @SafeVarargs
        @Override
        protected final ClusteringProcessResultHolder<T> doInBackground(QuadTree<T>... params) {
            System.out.println("start=="+SystemClock.elapsedRealtime());
            ClusteringProcessResultHolder<T> result = new ClusteringProcessResultHolder<T>();
            QuadTree<T> tree = params[0];
            System.out.println("tree=");
            // Store old points
            List<T> pointsToKeep = new ArrayList<T>(markers.keySet());
            List<T> pointsToDelete = new ArrayList<T>(markers.keySet());

            // Get x1,y1,xf,yf from bounds
            double x1 = Math.min(bounds.southwest.latitude, bounds.northeast.latitude);
            double y1 = Math.min(bounds.northeast.longitude, bounds.southwest.longitude);
            double xf = Math.max(bounds.southwest.latitude, bounds.northeast.latitude);
            double yf = Math.max(bounds.northeast.longitude, bounds.southwest.longitude);

            QuadTreeBoundingBox boundingBox = new QuadTreeBoundingBox(x1, y1, xf, yf);
            ArrayList<T> pointsInRegion = tree.getPointsInRange(boundingBox);

            // We got here the points we want to show
            result.pois.addAll(pointsInRegion);

            // Intersect the new points with the old points = get the points NOT TO delete
            //取交集
            pointsToKeep.retainAll(pointsInRegion);

            // Remove from the old points the ones we don't want to delete = in here we will have everything not showing

            pointsToDelete.removeAll(pointsToKeep);

            // Create all the Clusters
            HashMap<Point, Cluster<T>> positions = new HashMap<Point, Cluster<T>>();
            for (T point : pointsInRegion) {

                Point position = projection.toScreenLocation(point.getPosition());
                boolean addedToCluster = false;
                if (performCluster) {
                    for (Point storedPoint : positions.keySet()) {

                        if (isInDistance(position, storedPoint)) {
                            positions.get(storedPoint).addMarker(point);
                            addedToCluster = true;
                            break;
                        }
                    }
                }
                if (!addedToCluster) {
                    positions.put(position, new Cluster<T>(point));
                }
            }

            // Prepare the result: the pois to delete and the new clusters
            result.poisToDelete.addAll(pointsToDelete);
            result.poisToKeep.addAll(pointsToKeep);
            for (Cluster<T> cluster : positions.values()) {
                if (cluster.isCluster()) {
                    result.clusters.add(cluster);
                    for (T poi : cluster.getMarkers()) {
                        result.pois.remove(poi);
                        result.poisToKeep.remove(poi);
                        result.poisToDelete.add(poi);
                    }
                }
            }
            for (Cluster<T> cluster : clusterMarkers.values()) {
                if (result.clusters.contains(cluster)) {
                    result.clustersToKeep.add(cluster);
                } else {
                    result.clustersToDelete.add(cluster);
                }
            }
            System.out.println("end="+SystemClock.elapsedRealtime());
            return (isCancelled()) ? null : result;
        }

        @Override
        protected void onPostExecute(ClusteringProcessResultHolder<T> result) {

            if (result == null) return;

            // TODO I have to clean this mess eventually
            updatingLock.lock();

            // Remove all cluster marks (they will be regenerated)
            List<OverlayOptions> deletedClusters = new ArrayList<>();
            for (OverlayOptions marker : clusterMarkers.keySet()) {

                Cluster<T> cluster = clusterMarkers.get(marker);
                if (result.clustersToDelete.contains(cluster)) {
                    manager.cleanMarker(marker);
                    deletedClusters.add(marker);
                }
            }

            // Delete clusters marked for deletion
            for (OverlayOptions marker : deletedClusters) {
                clusterMarkers.remove(marker);
            }

            // Mark for deletion all the pois that wont be shown in the map
            List<T> deleted = new ArrayList<T>();
            for (T poi : result.poisToDelete) {
                OverlayOptions marker = markers.get(poi);
                if (marker != null) {
                    manager.cleanMarker(marker);
                }
                deleted.add(poi);
            }

            // Fixes for possible errors
            for (T poi : markers.keySet()) {
                if (!result.pois.contains(poi)) {
                    OverlayOptions marker = markers.get(poi);
                    if (marker != null) {
                        manager.cleanMarker(marker);
                    }
                    deleted.add(poi);
                }
            }

            // Actually remove the non shown pois
            for (T poi : deleted) {
                OverlayOptions marker = markers.remove(poi);
            }

            // Retrieve the map from the weak reference to operate with it
            BaiduMap strongMap = map.get();

            if (strongMap == null) return;
            manager.cleanAllMarker();
            ArrayList<OverlayOptions> newlyAddedMarkers = new ArrayList<>();

            // Generate all the clusters
            for (Cluster<T> cluster : result.clusters) {
                if (!result.clustersToKeep.contains(cluster)) {
                    MarkerOptions marker;
                    if (onPaintingCluster != null) {

                        marker =onPaintingCluster.onCreateClusterMarkerOptions(cluster);
                        marker.animateType(MarkerOptions.MarkerAnimateType.drop);
                        onPaintingCluster.onMarkerCreated(marker, cluster);
                    } else {
                        marker = new MarkerOptions().position(cluster.getCenter());
                                marker.title(Integer.valueOf(cluster.getWeight()).toString())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka))
                                .animateType(MarkerOptions.MarkerAnimateType.drop);
                    }
                    manager.addMarker(marker);
                    newlyAddedMarkers.add(marker);
                    clusterMarkers.put(marker, cluster);
                }
            }

            // Generate all the pois
            for (T poi : result.pois) {
                if (!markers.containsKey(poi)) {
                    MarkerOptions marker;
                    if (onPaintingClusterableMarker != null) {
                        marker = onPaintingClusterableMarker.onCreateMarkerOptions(poi);
                        marker.animateType(MarkerOptions.MarkerAnimateType.drop);
                        manager.addMarker(marker);
                        onPaintingClusterableMarker.onMarkerCreated(marker, poi);
                    } else {
                        marker = new MarkerOptions().position(poi.getPosition()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka));
                        marker.animateType(MarkerOptions.MarkerAnimateType.drop);
                        manager.addMarker(marker);
                    }
                    newlyAddedMarkers.add(marker);
                    markers.put(poi, marker);
                }
            }

            // Animate the new additions
            if (animationEnabled) {
                if (markerAnimation != null) {
                    animateRecentlyAddedMarkers(newlyAddedMarkers, markerAnimation);
                } else {
                    throw new RuntimeException("If animation is enabled, you should provide a MarkerAnimation");
                }
            }

            // Save bounds
            oldBounds = bounds;
            System.out.println("添加Marker");
            System.out.println(manager.getOverlayOptions().size());
            manager.addToMap();
           // manager.zoomToSpan();
            updatingLock.unlock();
        }
    }

    private void animateRecentlyAddedMarkers(final List<OverlayOptions> newlyAddedMarkers, final MarkerAnimation animation) {
        final long start = SystemClock.uptimeMillis();
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = animationInterpolator.getInterpolation((float) elapsed / animationDuration);

                for (OverlayOptions marker : newlyAddedMarkers) {
                    animation.animateMarker(marker, t);
                }

                if (t < 1.0) {
                    handler.postDelayed(this, 16); // 16ms = 60fps
                }
            }
        });

    }

    public T getClusterableFromMarker(Marker marker) {
        return markers.getKey(marker);
    }

    public OverlayOptions getMarkerFromClusterable(T clusterable) {
        return markers.get(clusterable);
    }

    public Interpolator getAnimationInterpolator() {
        return animationInterpolator;
    }

    public void setAnimationInterpolator(Interpolator animationInterpolator) {
        this.animationInterpolator = animationInterpolator;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public MarkerAnimation getMarkerAnimation() {
        return markerAnimation;
    }

    public void setMarkerAnimation(MarkerAnimation markerAnimation) {
        this.markerAnimation = markerAnimation;
    }

    private class ClusteringProcessResultHolder<T extends Clusterable> {
        public ArrayList<Cluster<T>> clusters = new ArrayList<>();
        public ArrayList<Cluster<T>> clustersToDelete = new ArrayList<>();
        public ArrayList<Cluster<T>> clustersToKeep = new ArrayList<>();
        public ArrayList<T> pois = new ArrayList<T>();
        public ArrayList<T> poisToDelete = new ArrayList<T>();
        public ArrayList<T> poisToKeep = new ArrayList<T>();
    }

    public interface OnCameraChangeListener {
        void onCameraChange(MapStatus position);
    }

    public interface ClustererClickListener<T extends Clusterable> {

        void markerClicked(T marker);

        void clusterClicked(Cluster position);
    }

}
