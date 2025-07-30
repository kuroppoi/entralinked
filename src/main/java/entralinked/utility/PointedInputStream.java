package entralinked.utility;

import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PointedInputStream extends FilterInputStream {
    private long pointer = 0;

    public PointedInputStream(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read != -1) pointer += 1;
        return read;
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        int bytes = super.read(b, off, len);
        pointer += bytes;
        return bytes;
    }

    public long skipTo(long n) throws IOException {
        return skip(n - pointer);
    }

    @Override
    public long skip(long n) throws IOException {
        long bytes = super.skip(n);
        pointer += bytes;
        return bytes;
    }

    @Override
    public void reset() throws IOException {
        pointer = 0;
        super.reset();
    }
}
