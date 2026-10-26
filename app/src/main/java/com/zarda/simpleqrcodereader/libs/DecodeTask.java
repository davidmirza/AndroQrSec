package com.zarda.simpleqrcodereader.libs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zarda.simpleqrcodereader.MultiFormatReader;
import com.zarda.simpleqrcodereader.PlanarYUVLuminanceSource;
import com.zarda.simpleqrcodereader.ReaderException;
import com.zarda.simpleqrcodereader.Result;


final class DecodeTask {
    private final byte[] mImage;
    private final Point mImageSize;
    private final Point mPreviewSize;
    private final Point mViewSize;
    private final Rect mViewFrameRect;
    private final int mOrientation;
    private final boolean mReverseHorizontal;

    public DecodeTask(@NonNull final byte[] image, @NonNull final Point imageSize,
                      @NonNull final Point previewSize, @NonNull final Point viewSize,
                      @NonNull final Rect viewFrameRect, final int orientation,
                      final boolean reverseHorizontal) {
        mImage = image;
        mImageSize = imageSize;
        mPreviewSize = previewSize;
        mViewSize = viewSize;
        mViewFrameRect = viewFrameRect;
        mOrientation = orientation;
        mReverseHorizontal = reverseHorizontal;
    }
    @Nullable
    @SuppressWarnings("SuspiciousNameCombination")
    public Result decode(@NonNull final MultiFormatReader reader) throws ReaderException {
        int imageWidth = mImageSize.getx();
        int imageHeight = mImageSize.gety();
        final int orientation = mOrientation;
        final byte[] image = Utils.rotateYuv(mImage, imageWidth, imageHeight, orientation);
        if (orientation == 90 || orientation == 270) {
            final int width = imageWidth;
            imageWidth = imageHeight;
            imageHeight = width;
        }
        final Rect frameRect =
                Utils.getImageFrameRect(imageWidth, imageHeight, mViewFrameRect, mPreviewSize,
                        mViewSize);
        final int frameWidth = frameRect.getWidth();
        final int frameHeight = frameRect.getHeight();
        if (frameWidth < 1 || frameHeight < 1) {
            return null;
        }
        return Utils.decodeLuminanceSource(reader,
                new PlanarYUVLuminanceSource(image, imageWidth, imageHeight, frameRect.getmLeft(),
                        frameRect.getmTop(), frameWidth, frameHeight, mReverseHorizontal));
    }
}
