package com.zarda.simpleqrcodereader.libs;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zarda.simpleqrcodereader.BarcodeFormat;
import com.zarda.simpleqrcodereader.DecodeHintType;
import com.zarda.simpleqrcodereader.EncodeHintType;
import com.zarda.simpleqrcodereader.MultiFormatReader;
import com.zarda.simpleqrcodereader.MultiFormatWriter;
import com.zarda.simpleqrcodereader.PlanarYUVLuminanceSource;
import com.zarda.simpleqrcodereader.RGBLuminanceSource;
import com.zarda.simpleqrcodereader.ReaderException;
import com.zarda.simpleqrcodereader.Result;
import com.zarda.simpleqrcodereader.WriterException;
import com.zarda.simpleqrcodereader.common.BitMatrix;

public class BarcodeUtils {

    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    private BarcodeUtils() {
    }
    @Nullable
    public static Result decodeBitmap(@NonNull final Bitmap bitmap) {
        return decodeBitmap(bitmap, null);
    }
    @Nullable
    public static Result decodeBitmap(@NonNull final Bitmap bitmap,
                                      @Nullable final Map<DecodeHintType, ?> hints) {
        Objects.requireNonNull(bitmap);
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        return decodeRgb(pixels, width, height, hints);
    }
    @Nullable
    public static Result decodeRgb(@NonNull final int[] pixels, final int width, final int height) {
        return decodeRgb(pixels, width, height, null);
    }
    @Nullable
    public static Result decodeRgb(@NonNull final int[] pixels, final int width, final int height,
                                   @Nullable final Map<DecodeHintType, ?> hints) {
        Objects.requireNonNull(pixels);
        final MultiFormatReader reader = createReader(hints);
        try {
            return Utils
                    .decodeLuminanceSource(reader, new RGBLuminanceSource(width, height, pixels));
        } catch (final ReaderException e) {
            return null;
        }
    }
    @Nullable
    public static Result decodeYuv(@NonNull final byte[] pixels, final int width,
                                   final int height) {
        return decodeYuv(pixels, width, height, ROTATION_0, false, null);
    }
    @Nullable
    @SuppressWarnings("SuspiciousNameCombination")
    public static Result decodeYuv(@NonNull final byte[] pixels, final int width, final int height,
                                   @Rotation final int rotation, final boolean reverseHorizontal,
                                   @Nullable final Map<DecodeHintType, ?> hints) {
        Objects.requireNonNull(pixels);
        final byte[] rotatedPixels = Utils.rotateYuv(pixels, width, height, rotation);
        final int rotatedWidth;
        final int rotatedHeight;
        if (rotation == ROTATION_90 || rotation == ROTATION_270) {
            rotatedWidth = height;
            rotatedHeight = width;
        } else {
            rotatedWidth = width;
            rotatedHeight = height;
        }
        final MultiFormatReader reader = createReader(hints);
        try {
            return Utils.decodeLuminanceSource(reader,
                    new PlanarYUVLuminanceSource(rotatedPixels, rotatedWidth, rotatedHeight, 0, 0,
                            rotatedWidth, rotatedHeight, reverseHorizontal));
        } catch (final ReaderException e) {
            return null;
        }
    }
    @Nullable
    public static BitMatrix encodeBitMatrix(@NonNull final String content,
                                            @NonNull final BarcodeFormat format, final int width, final int height) {
        return encodeBitMatrix(content, format, width, height, null);
    }
    @Nullable
    public static BitMatrix encodeBitMatrix(@NonNull final String content,
                                            @NonNull final BarcodeFormat format, final int width, final int height,
                                            @Nullable final Map<EncodeHintType, ?> hints) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(format);
        final MultiFormatWriter writer = new MultiFormatWriter();
        try {
            if (hints != null) {
                return writer.encode(content, format, width, height, hints);
            } else {
                return writer.encode(content, format, width, height);
            }
        } catch (final WriterException e) {
            return null;
        }
    }
    @Nullable
    public static Bitmap encodeBitmap(@NonNull final String content,
                                      @NonNull final BarcodeFormat format, final int width, final int height) {
        return encodeBitmap(content, format, width, height, null);
    }
    @Nullable
    public static Bitmap encodeBitmap(@NonNull final String content,
                                      @NonNull final BarcodeFormat format, final int width, final int height,
                                      @Nullable final Map<EncodeHintType, ?> hints) {
        final BitMatrix matrix = encodeBitMatrix(content, format, width, height, hints);
        if (matrix != null) {
            return createBitmap(matrix);
        } else {
            return null;
        }
    }
    @NonNull
    public static Bitmap createBitmap(@NonNull final BitMatrix matrix) {
        Objects.requireNonNull(matrix);
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        final int length = width * height;
        final int[] pixels = new int[length];
        for (int i = 0; i < length; i++) {
            pixels[i] = matrix.get(i % width, i / height) ? Color.BLACK : Color.WHITE;
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }
    @NonNull
    private static MultiFormatReader createReader(@Nullable final Map<DecodeHintType, ?> hints) {
        final MultiFormatReader reader = new MultiFormatReader();
        if (hints != null) {
            reader.setHints(hints);
        } else {
            reader.setHints(Collections
                    .singletonMap(DecodeHintType.POSSIBLE_FORMATS, CodeScanner.ALL_FORMATS));
        }
        return reader;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270})
    public @interface Rotation {
    }

}
