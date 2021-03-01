package pt.ulisboa.tecnico.cnv.policies;

public class LoadComplexity {
    public static enum COMPLEXITIES {
        MIN(0);

        public LoadComplexity value;
        private COMPLEXITIES(int value)
        {
            this.value = new LoadComplexity(value);
        }
    }

    private long complexity;
    private long progress;

    public LoadComplexity(long compl) {
        complexity = compl;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public LoadComplexity add(LoadComplexity other){
        this.complexity += other.complexity;
        return this;
    }

    public LoadComplexity sub(LoadComplexity other){
        this.complexity -= other.complexity;
        return this;
    }

    public long getComplexity() {
        return Long.max(complexity - progress, 0);
    }

    public void setComplexity(long complexity) {
        this.complexity = complexity;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public boolean lt(LoadComplexity other) {
            return this.complexity < other.complexity;
    }

    public boolean gt(LoadComplexity other) {
        return this.complexity > other.complexity;
    }

    public boolean lte(LoadComplexity other) {
            return this.complexity <= other.complexity;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof LoadComplexity) {
            return this.complexity == ((LoadComplexity)other).complexity;
        }
        return false;
    }
}
