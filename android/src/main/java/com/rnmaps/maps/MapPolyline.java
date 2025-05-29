package com.rnmaps.maps;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.PolylineOptions.LineJoinType;
import com.amap.api.maps.model.PolylineOptions.LineCapType;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.maps.model.StrokeStyle;
import com.google.android.gms.maps.model.StyleSpan;
import com.google.maps.android.collections.PolylineManager;
import com.rnmaps.fabric.event.OnPressEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapPolyline extends MapFeature {

    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private com.amap.api.maps.model.PolylineOptions amapPolylineOptions;
    private com.amap.api.maps.model.Polyline amapPolyline;

    private List<LatLng> coordinates;
    private List<com.amap.api.maps.model.LatLng> amapCoordinates;
    private int color;
    private float width;
    private boolean tappable;
    private boolean geodesic;
    private float zIndex;
    private Cap lineCap = new RoundCap();
    private LineCapType amapLineCap = LineCapType.LineCapRound;
    private ReadableArray patternValues;
    private ReadableArray rawCoordinates;
    private List<PatternItem> pattern;
    private String lineJoin;

    private List<StyleSpan> spans;

    public MapPolyline(Context context) {
        super(context);
    }


    private void buildCoordinates(ReadableArray coordinates) {
        this.coordinates = new ArrayList<>(coordinates.size());
        for (int i = 0; i < coordinates.size(); i++) {
            ReadableMap coordinate = coordinates.getMap(i);
            this.coordinates.add(i,
                    new LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")));
        }
    }

    private void buildAMapCoordinates(ReadableArray coordinates) {
        this.amapCoordinates = new ArrayList<>(coordinates.size());
        for (int i = 0; i < coordinates.size(); i++) {
            ReadableMap coordinate = coordinates.getMap(i);
            this.amapCoordinates.add(i,
                    new com.amap.api.maps.model.LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")));
        }
    }

    public void setCoordinates(ReadableArray coordinates) {
        if (polyline != null) {
            buildCoordinates(coordinates);
            polyline.setPoints(this.coordinates);
        } else if (amapPolyline != null) {
            buildAMapCoordinates(coordinates);
            amapPolyline.setPoints(this.amapCoordinates);
        } else {
            this.rawCoordinates = coordinates;
        }
    }

    public void setColor(int color) {
        this.color = color;
        if (polyline != null) {
            polyline.setColor(color);
        } else if (amapPolyline != null) {
            amapPolyline.setColor(color);
        }
    }

    public void setStrokeColors(ReadableArray strokeColors) {
        if (polyline != null) {
            List<StyleSpan> spans = new ArrayList<>();
            for (int i = 0; i < strokeColors.size(); i++) {
                StrokeStyle stroke;

                if (i == 0) {
                    stroke = StrokeStyle.colorBuilder(strokeColors.getInt(i)).build();
                } else {
                    stroke = StrokeStyle.gradientBuilder(strokeColors.getInt(i - 1), strokeColors.getInt(i)).build();
                }
                spans.add(new StyleSpan(stroke));
            }
            this.spans = spans;
            polyline.setSpans(spans);
        }
    }

    public void setWidth(float width) {
        this.width = width;
        if (polyline != null) {
            polyline.setWidth(width);
        } else if (amapPolyline != null) {
            amapPolyline.setWidth(width);
        }
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (polyline != null) {
            polyline.setZIndex(zIndex);
        } else if (amapPolyline != null) {
            amapPolyline.setZIndex(zIndex);
        }
    }

    public void setTappable(boolean tapabble) {
        this.tappable = tapabble;
        if (polyline != null) {
            polyline.setClickable(tappable);
        }
        // AMap not support
    }

    public void setGeodesic(boolean geodesic) {
        this.geodesic = geodesic;
        if (polyline != null) {
            polyline.setGeodesic(geodesic);
        } else if (amapPolyline != null) {
            amapPolyline.setGeodesic(geodesic);
        }
    }

    private void setLineCap(Cap cap) {
        if (polyline != null) {
            polyline.setStartCap(cap);
            polyline.setEndCap(cap);
        } else if (amapPolyline != null) {
            amapPolylineOptions.lineCapType(amapLineCap);
            amapPolyline.setOptions(amapPolylineOptions);
        }
        this.applyPattern();
    }

    public void setLineDashPattern(ReadableArray patternValues) {
        this.patternValues = patternValues;
        this.applyPattern();
    }

    private void applyPattern() {
        if (polyline == null || patternValues == null) {
            return;
        }
        this.pattern = new ArrayList<>(patternValues.size());
        for (int i = 0; i < patternValues.size(); i++) {
            float patternValue = (float) patternValues.getDouble(i);
            boolean isGap = i % 2 != 0;
            if (isGap) {
                this.pattern.add(new Gap(patternValue));
            } else {
                PatternItem patternItem;
                boolean isLineCapRound = this.lineCap instanceof RoundCap;
                if (isLineCapRound) {
                    patternItem = new Dot();
                } else {
                    patternItem = new Dash(patternValue);
                }
                this.pattern.add(patternItem);
            }
        }
        if (polyline != null) {
            polyline.setPattern(this.pattern);
        }
    }

    public PolylineOptions getPolylineOptions() {
        if (polylineOptions == null) {
            polylineOptions = createPolylineOptions();
        }
        return polylineOptions;
    }
    public com.amap.api.maps.model.PolylineOptions getAMapPolylineOptions() {
        if (amapPolylineOptions == null) {
            amapPolylineOptions = createAMapPolylineOptions();
        }
        return amapPolylineOptions;
    }
    private  PolylineOptions createPolylineOptions() {
        PolylineOptions options = new  PolylineOptions();
        if (rawCoordinates != null) {
            buildCoordinates(rawCoordinates);
            rawCoordinates = null;
        }
        options.addAll(coordinates);
        options.color(color);
        options.width(width);
        options.geodesic(geodesic);
        options.zIndex(zIndex);
        options.startCap(lineCap);
        options.endCap(lineCap);
        options.pattern(this.pattern);
        return options;
    }
    private  com.amap.api.maps.model.PolylineOptions createAMapPolylineOptions() {
        com.amap.api.maps.model.PolylineOptions options = new  com.amap.api.maps.model.PolylineOptions();
        if (rawCoordinates != null) {
            buildAMapCoordinates(rawCoordinates);
            rawCoordinates = null;
        }
        options.addAll(amapCoordinates);
        options.color(color);
        options.width(width);
        options.geodesic(geodesic);
        options.zIndex(zIndex);
        options.lineCapType(amapLineCap);
        if (lineJoin != null) {
            options.lineJoinType(buildAMapLineJoinType(lineJoin));
            lineJoin = null;
        }
        return options;
    }

    @Override
    public Object getFeature() {
        if (polyline != null) {
            return polyline;
        } else {
            return amapPolyline;
        }
    }

    @Override
    public void addToMap(Object collection) {
        if (collection instanceof PolylineManager.Collection polylineCollection) {
            polyline = polylineCollection.addPolyline(getPolylineOptions());
            polyline.setClickable(this.tappable);
            if (spans != null){
                polyline.setSpans(spans);
            }
        } else if (collection instanceof AMap) {
            ((AMap)collection).addPolyline(getAMapPolylineOptions());
        }
    }

    @Override
    public void removeFromMap(Object collection) {
        if (amapPolyline != null) {
            amapPolyline.remove();
        } else if (collection instanceof PolylineManager.Collection polylineCollection) {
            polylineCollection.remove(polyline);
        }
    }

    public void setLineCap(String lineCap) {
        Cap cap;
        switch (lineCap) {
            case "round":
                cap = new RoundCap();
                amapLineCap = LineCapType.LineCapRound;
                break;
            case "square":
                cap = new SquareCap();
                amapLineCap = LineCapType.LineCapSquare;
                break;
            case "butt":
            default:
                cap = new ButtCap();
                amapLineCap = LineCapType.LineCapButt;
                break;
        }
        this.lineCap = cap;
        setLineCap(cap);
    }

    public static Map<String, Object> getExportedCustomBubblingEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        builder.put(OnPressEvent.EVENT_NAME, MapBuilder.of("registrationName", OnPressEvent.EVENT_NAME));
        return builder.build();
    }

    private int buildLineJoinType(String lineJoin) {
        int type;
        switch (lineJoin) {
            case "round":
                type = JointType.ROUND;
                break;
            case "bevel":
                type = JointType.BEVEL;
                break;
            case "miter":
            default:
                type = JointType.DEFAULT;
                break;
        }
        return type;
    }

    private LineJoinType buildAMapLineJoinType(String lineJoin) {
        LineJoinType type;
        switch (lineJoin) {
            case "round":
                type = LineJoinType.LineJoinRound;
                break;
            case "bevel":
                type = LineJoinType.LineJoinBevel;
                break;
            case "miter":
            default:
                type = LineJoinType.LineJoinMiter;
                break;
        }
        return type;
    }

    public void setLineJoin(String lineJoin) {
        if (polyline != null) {
            polyline.setJointType(buildLineJoinType(lineJoin));
        } else if (amapPolyline != null) {
            amapPolylineOptions.lineJoinType(buildAMapLineJoinType(lineJoin));
            amapPolyline.setOptions(amapPolylineOptions);
        } else {
            this.lineJoin = lineJoin;
        }
    }
}
