package org.streampipes.examples.jvm.processor.denStream;

public class DenPoint {
    public float x;
    public float y;
    public long timestamp;
    public boolean covered;

    public DenPoint( float x, float y, long timestamp )
    {
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    public DenPoint(){
    }

    public DenPoint( float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public float[] toArray(){
        float[] p = new float[2];
        p [0] = this.x;
        p [1] = this.y;
        return p;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
}
