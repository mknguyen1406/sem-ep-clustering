package org.streampipes.examples.jvm.processor.denStream;

public class MicroCluster {
    private int id;
    public float[] LS;
    public float[] SS;
    public int N;
    public long lastEditT = -1;
    public float lambda;
    public long currentTimestamp;
    private long creationTimestamp = -1;
    float weight;

    public MicroCluster(float[] center, int dimensions, long creationTimestamp, float lambda, long currentTimestamp){
        this.creationTimestamp = creationTimestamp;
        this.lastEditT = creationTimestamp;
        this.lambda = lambda;
        this.currentTimestamp = currentTimestamp;
        this.N = 1;
        this.LS = center;
        this.SS = new float[dimensions];
        for (int i=0; i<SS.length; i++){
            SS[i]=center[i] * center[i];

        }
    }

    public void insert(DenPoint point, long timstamp) {
        N++;
        this.lastEditT = timstamp;

        // weight = N + 1;

        LS [0] += point.x;
        LS [1] += point.y;
        SS [0] += (point.x * point.x);
        SS [1] += (point.y * point.y);
    }

    public DenPoint getCenter(long timestamp, boolean micro){

        DenPoint result = new DenPoint();
        float[] resultF = new float[LS.length];

        if (micro) {
            //Include damped window model when calculating center for micro cluster
            float dt = timestamp - lastEditT;
            float w = getWeight(timestamp);
            for (int i = 0; i < LS.length; i++) {
                resultF[i] = LS[i];
                float fn = (float) Math.pow(2, -lambda * dt);

                resultF[i] *= fn;
                resultF[i] /= w;
            }
        } else {
            //Calculate simple center for macro cluster
            for (int i = 0; i < LS.length; i++) {
                resultF[i] = LS[i]/N;
            }
        }
        result.x = resultF[0];
        result.y = resultF[1];
        return result;
    }


    public float getWeight(){
        return getWeight (currentTimestamp);
    }

    public float getWeight(long timestamp) {
        float dt = timestamp - lastEditT;
        return (float)(N * Math.pow (2, -lambda * dt));
    }


    public float getRadius(long timestamp, boolean micro){

        float max = 0;

        if (micro) {
            long dt = timestamp - lastEditT;
            float[] cf1 = calcCF1(dt);
            float[] cf2 = calcCF2(dt);

            float w = getWeight(timestamp);

            for (int i = 0; i < SS.length; i++) {
                float x1 = cf2[i] / w;
                float temp = cf1[i] / w;
                float x2 = temp * temp;
                float diffSqrt = (float) Math.sqrt(x1 - x2);
                if (diffSqrt > max) {
                    max = diffSqrt;
                }
            }
        } else {
            for (int i = 0; i < SS.length; i++) {
                float x1 = SS[i];
                float temp = LS[i];
                float x2 = temp * temp;
                float diffSqrt = (float) Math.sqrt(x1 - x2); //TODO Check for negativity
                if (diffSqrt > max) {
                    max = diffSqrt;
                }
            }
        }

        return max;
    }

    public float[] calcCF1(long dt) {
        float[] cf1 = new float[LS.length];

        for (int i = 0; i < LS.length; i++) {
            cf1 [i] = (float)Math.pow (2, -lambda * dt) * LS [i];
        }
        return cf1;
    }

    public float[] calcCF2(long dt) {
        float[] cf2 = new float[SS.length];
        for (int i = 0; i < SS.length; i++) {
            cf2 [i] = (float)Math.pow (2, -lambda * dt) * SS [i];
        }
        return cf2;
    }

    public long getCreationTime() {
        return creationTimestamp;
    }

    public MicroCluster copy(){
        MicroCluster copy = new MicroCluster(this.LS.clone(), this.LS.length, this.getCreationTime(), this.lambda, this.currentTimestamp);
        copy.N = this.N;
        copy.SS = this.SS.clone();
        copy.LS = this.LS.clone();
        copy.lastEditT = this.lastEditT;
        return copy;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
