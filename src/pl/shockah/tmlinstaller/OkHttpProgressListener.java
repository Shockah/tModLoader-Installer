package pl.shockah.tmlinstaller;

public interface OkHttpProgressListener {
	void update(long bytesRead, long contentLength, boolean done);
}