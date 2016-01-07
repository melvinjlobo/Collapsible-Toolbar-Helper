package abysmel.com.collapsibletoolbarhelper.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.FloatRange;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import abysmel.com.collapsibletoolbarhelper.R;

/**
 * Created by Melvin Lobo on 11/21/2015.
 *
 * Adapted from AOSP's CollapsingTextHelper, Nick Butchers version of CollapsibleTitleLayout
 */
public class CollapsibleTextLayout extends FrameLayout {

	// /////////////////////////////////////// CLASS MEMBERS ////////////////////////////////////////
	private static final int		COLLAPSED_TEXT_SIZE		= 32;
	private static final int		EXPANDED_TEXT_SIZE		= 62;
	private final Rect				mExpandedBounds;
	private final Rect				mCollapsedBounds;
	private float					mfCollapsedTextSize		= 0.0f;
	private float					mfExpandedTextSize		= 0.0f;
	private float					mfMaxExpandedTextSize	= 0.0f;
	private int						mExpandedTextColor		= Color.WHITE;
	private int						mCollapsedTextColor		= Color.WHITE;
	private CharSequence			msText					= "StoreTools";
	private SpannableStringBuilder	msDisplayText;
	private TextPaint				mTextPaint;
	private int						mnCalculatedWithWidth	= 0;
	private int						mnCalculatedWithHeight	= 0;
	private StaticLayout			mStaticMeasuringLayout	= null;
	private StaticLayout			mStaticSpannableLayout	= null;
	private float					mExpandedFraction		= 0.0f;
	private float					mExpandedDrawY			= 0.0f;
	private float					mCollapsedDrawY			= 0.0f;
	private float					mExpandedDrawX			= 0.0f;
	private float					mCollapsedDrawX			= 0.0f;
	private float					mCurrentDrawX			= 0.0f;
	private float					mCurrentDrawY			= 0.0f;
	private CharSequence			mTextToDraw				= "";
	private float					mScale					= 0.0f;
	private float					mCurrentTextSize		= 0.0f;
	private boolean					mBoundsChanged			= false;
	private Typeface				mPrimaryTypeface		= null;
	private Typeface				mSecondaryTypeface		= null;
	private boolean					mbShouldDrawTitle		= false;
	private boolean					mbDrawSpannable			= false;
	private boolean					mbIsMultifacetedString	= false;
	private int						mnSplitStringAtPosition = 0;
	private Interpolator 			mSizeInterpolator 		= null;

	// /////////////////////////////////////// CLASS METHODS ////////////////////////////////////////

	/**
	 * Constructor to inflate the custom widget. The Android system calls the appropriate constructor
	 *
	 * @param context
	 *            The context of the activity which acts as a parent to the widget
	 *
	 * @author Melvin Lobo
	 */
	public CollapsibleTextLayout(Context context) {
		this(context, null);
	}

	/**
	 * Constructor to inflate the custom widget. The Android system calls the appropriate constructor
	 *
	 * @param context
	 *            The context of the activity which acts as a parent to the widget
	 * @param attrs
	 *            The custom attributes associated with this widget and defined in the xml
	 *
	 * @author Melvin Lobo
	 */
	public CollapsibleTextLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Constructors to inflate the custom widget. The Android system calls the appropriate constructor
	 *
	 * @param context
	 *            The context of the activity which acts as a parent to the widget
	 * @param attrs
	 *            The custom attributes associated with this widget and defined in the xml
	 * @param defStyle
	 *            The default style to be applied
	 *
	 * @author Melvin Lobo
	 */
	public CollapsibleTextLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Let the parent know that we will draw
		setWillNotDraw(false);

		init(context, attrs);

		// Initialize the bounds
		mCollapsedBounds = new Rect();
		mExpandedBounds = new Rect();

		//Initialize the interpolator
		mSizeInterpolator = new DecelerateInterpolator();
	}

	/**
	 * Initialize the values by getting it from the attributes
	 *
	 * @param context
	 *            The context of the activity which acts as a parent to the widget
	 * @param attrs
	 *            The custom attributes associated with this widget and defined in the xml
	 *
	 * @author Melvin Lobo
	 */
	private void init(Context context, AttributeSet attrs) {
		if(attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CollapsibleTextLayout, 0, 0);

			msText = a.getString(R.styleable.CollapsibleTextLayout_textToShow);
			msDisplayText = new SpannableStringBuilder(msText);
			mExpandedTextColor = a.getColor(R.styleable.CollapsibleTextLayout_expandedTextColor, ContextCompat.getColor(context, android.R.color.white));
			mCollapsedTextColor = a.getColor(R.styleable.CollapsibleTextLayout_collapsedTextColor, ContextCompat.getColor(context, android.R.color.white));
			String typeFace = a.getString(R.styleable.CollapsibleTextLayout_typefaceFamilyPrimary);
			mPrimaryTypeface = Typeface.create(((typeFace == null) ? "sans-serif-medium" : typeFace), Typeface.NORMAL);
			typeFace = a.getString(R.styleable.CollapsibleTextLayout_typefaceFamilySecondary);
			mSecondaryTypeface = Typeface.create(((typeFace == null) ? "sans-serif-medium" : typeFace), Typeface.NORMAL);
			mfCollapsedTextSize = a.getDimension(R.styleable.CollapsibleTextLayout_collapsedTextSize, d2x(COLLAPSED_TEXT_SIZE));
			mfMaxExpandedTextSize = a.getDimension(R.styleable.CollapsibleTextLayout_maxExpandedTextSize, d2x(EXPANDED_TEXT_SIZE));
			mfExpandedTextSize = a.getDimension(R.styleable.CollapsibleTextLayout_expandedTextSize, d2x(EXPANDED_TEXT_SIZE));
			mbIsMultifacetedString = a.getBoolean(R.styleable.CollapsibleTextLayout_isMultiFaceted, false);
			mnSplitStringAtPosition = a.getInteger(R.styleable.CollapsibleTextLayout_typefaceSplitPosition, 0);

			//Initialize Paint values
			mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			mTextPaint.setColor(mExpandedTextColor);
			mTextPaint.setTypeface(mPrimaryTypeface);
			mTextPaint.setTextSize(mfCollapsedTextSize);
			mbDrawSpannable = false;

			a.recycle();
		}
	}

	/**
	 * Recursive binary search to find the best size for the text.
	 *
	 * Adapted from https://github.com/grantland/android-autofittextview
	 *
	 * @author Melvin Lobo
	 */
	public static float getSingleLineTextSize(String text, TextPaint paint, float targetWidth, float low, float high, float precision,
			DisplayMetrics metrics) {

		// Find the mid
		final float mid = (low + high) / 2.0f;

		// Get the maximum text width for the Mid
		paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, metrics));
		final float maxLineWidth = paint.measureText(text);

		// If the value is not close to precision, based on if its greater than or less than the target width,
		// we move to either side of the scle divided by the mid and repeat the process again
		if ((high - low) < precision) {
			return low;
		} else if (maxLineWidth > targetWidth) {
			return getSingleLineTextSize(text, paint, targetWidth, low, mid, precision, metrics);
		} else if (maxLineWidth < targetWidth) {
			return getSingleLineTextSize(text, paint, targetWidth, mid, high, precision, metrics);
		} else {
			return mid;
		}
	}

	/**
	 * Adapted from AnimationUtils. This function calculates the linear interpolation between the
     * start value and end value by fraction
	 *
	 * @param startValue
	 *            The start value
	 * @param endValue
	 *            The end value
	 * @param fraction
	 *            The fraction to be used for calculation
	 * @return The interpolated value between the start and the end based on the fraction
	 *
	 * @author Melvin Lobo
	 */
	private float interpolate(float startValue, float endValue, float fraction) {
		if(mSizeInterpolator != null)
			fraction = mSizeInterpolator.getInterpolation(fraction);

		return (startValue + (fraction * (endValue - startValue)));
	}

	/**
	 * Check if a rect edges are equal to changed values
	 *
	 * @param r
	 *            Rect 1 to be compared to
	 * @param left
	 *            The changed left
	 * @param top
	 *            Teh changed top
	 * @param right
	 *            The changed right
	 * @param bottom
	 *            The changed bottom
	 * @return true if the rect edge values are equal to the changed values, false if not
	 *
	 * @author Melvin Lobo
	 */
	private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
		return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
	}

	/**
	 * Returns true if value is close to target value.
	 *
	 * @return True if it's difference is < 0.001, false otherwise
	 *
	 * @author Melvin Lobo
	 */
	private static boolean isClose(float value, float targetValue) {
		return Math.abs(value - targetValue) < 0.001f;
	}

	/**
	 * Blend {@code color1} and {@code color2} using the given ratio.
	 *
	 * @param ratio
	 *            of which to blend. 0.0 will return {@code color1}, 0.5 will give an even blend,
     *            1.0 will return {@code color2}.
	 *
	 */
	private static int blendColors(int color1, int color2, float ratio) {
		final float inverseRatio = 1f - ratio;
		float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
		float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
		float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
		float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
		return Color.argb((int) a, (int) r, (int) g, (int) b);
	}

	/**
	 * Set the text
	 *
	 * @param sText
	 *            The text to be shown
	 *
	 * @author Melvin Lobo
	 */
	public void setText(CharSequence sText) {
		msText = sText;
		msDisplayText = new SpannableStringBuilder(msText);
	}

	/**
	 * Set the fraction based on which the text will scale and move towards the collapsed / expanded bounds
	 *
	 * @param offset
	 *            The fraction to be used to calculate the current bounds
	 *
	 * @author Melvin Lobo
	 */
	public void setScrollOffsetFraction(@FloatRange(from = 0f, to = 1f) float offset) {
		offset = offset < 0f ? 0f : (offset > 1f ? 1f : offset);
		if (mExpandedFraction != offset) {
			mExpandedFraction = offset;
			calculateCurrentOffsets();
		}
	}

	/**
	 * Set the expanded bounds
	 * 
	 * @param left
	 *            The left edge for the bound
	 * @param top
	 *            The top edge for the bound
	 * @param right
	 *            The right edge for the bound
	 * @param bottom
	 *            The bottom edge for the bound
	 *
	 * @author Melvin Lobo
	 */
	public void setExpandedBounds(int left, int top, int right, int bottom) {
		if (!rectEquals(mExpandedBounds, left, top, right, bottom)) {
			mExpandedBounds.set(left, top, right, bottom);
			mBoundsChanged = true;
			onBoundsChanged();
		}
	}

	/**
	 * Set the collapsed bounds
	 * 
	 * @param left
	 *            The left edge for the bound
	 * @param top
	 *            The top edge for the bound
	 * @param right
	 *            The right edge for the bound
	 * @param bottom
	 *            The bottom edge for the bound
	 *
	 * @author Melvin Lobo
	 */
	public void setCollapsedBounds(int left, int top, int right, int bottom) {
		if (!rectEquals(mCollapsedBounds, left, top, right, bottom)) {
			mCollapsedBounds.set(left, top, right, bottom);
			mBoundsChanged = true;
			onBoundsChanged();
		}
	}

	/**
	 * Note if the bounds change. it means we need to re-draw the text
	 *
	 * @author Melvin Lobo
	 */
	private void onBoundsChanged() {
		mbShouldDrawTitle = mCollapsedBounds.width() > 0 && mCollapsedBounds.height() > 0 && mExpandedBounds.width() > 0
				&& mExpandedBounds.height() > 0;
	}

	/**
	 * Calculate the Base offsets based on gravity. We set the expanded text vertical gravity to BOTTOM
     * and calculate accordingly. Alternatively, we
	 * assume the vertical gravity of the collapsed text to be CENTER_VERTICAL. The horizontal gravities
     * of both expanded and collapsed text are
	 * assumed to be LEFT
     * ToDO - Get gravity that the user wants for both horizontal and vertical alignments for both texts
     * ToDo - like AOSP and calculate the offsets
	 *
	 * @author Melvin Lobo
	 */
	private void calculateBaseOffsets() {
		// final float currentTextSize = mCurrentTextSize;

		// Collapsed text would have gravity center_vertical
		float textHeight = mTextPaint.descent() - mTextPaint.ascent();
		float textOffset = (textHeight / 2) - mTextPaint.descent();
		mCollapsedDrawY = mCollapsedBounds.centerY() + textOffset;
		mCollapsedDrawX = mCollapsedBounds.left;

		// Expanded text would have gravity bottom
		mExpandedDrawY = mExpandedBounds.bottom - 1;		//Need the 1 pixel as otherwise, it moves below bounds
		mExpandedDrawX = mExpandedBounds.left;
	}

	/**
	 * Calculate the current offsets. These are interpolated co-ordinates that draw will use to draw the text.
     * It also sets the interpolated text size
	 * of the paint to draw or a canvas scaling factor if the text size is between collapsed and expanded
     * versions. Moreover, if the text colors for
	 * expanded and collapsed texts are different, it will blend them.
	 *
	 * @author Melvin Lobo
	 */
	private void calculateCurrentOffsets() {
		final float fraction = mExpandedFraction;
		mCurrentDrawX = interpolate(mExpandedDrawX, mCollapsedDrawX, fraction);
		mCurrentDrawY = interpolate(mExpandedDrawY, mCollapsedDrawY, fraction);
		setInterpolatedValues(interpolate(mfExpandedTextSize, mfCollapsedTextSize, fraction));
		if (mCollapsedTextColor != mExpandedTextColor) {
			// If the collapsed and expanded text colors are different, blend them based on the
			// fraction
			mTextPaint.setColor(blendColors(mExpandedTextColor, mCollapsedTextColor, fraction));
		} else {
			mTextPaint.setColor(mCollapsedTextColor);
		}
		ViewCompat.postInvalidateOnAnimation(this);
	}

	/**
	 * The font size is scaled to Collapsed / Expanded Text Size if its anywhere close to these values.
     * Else, the canvas is scaled in draw with the
	 * factor calculated here.
     * Adapted from CollapsingTextHelper, AOSP.
	 *
	 * @param textSize
	 *            The text size to be used for calculation
	 *
	 * @author Melvin Lobo
	 */
	private void calculateUsingTextSize(final float textSize) {
		if (msText == null)
			return;

		final float availableWidth;
		final float newTextSize;
		boolean updateDrawText = false;

		// Check if we are close to the collapsed or Expanded text size
		// and decide if we need to set the scale to 1 or a fraction based on the current offset value
		if (isClose(textSize, mfCollapsedTextSize)) {
			availableWidth = mCollapsedBounds.width();
			newTextSize = mfCollapsedTextSize;
			mScale = 1f;
		} else {
			availableWidth = mExpandedBounds.width();
			newTextSize = mfExpandedTextSize;

			if (isClose(textSize, mfExpandedTextSize)) {
				// If we're close to the expanded text size, snap to it and use a scale of 1
				mScale = 1f;
			} else {
				// Else, we'll scale down from the expanded text size
				mScale = textSize / mfExpandedTextSize;
			}
		}

		// Check if we need to update the text
		if (availableWidth > 0) {
			updateDrawText = (mCurrentTextSize != newTextSize) || mBoundsChanged || updateDrawText;
			mCurrentTextSize = newTextSize;
			mBoundsChanged = false;
		}

		// Need to draw text
		if (mTextToDraw == null || updateDrawText) {
			mTextPaint.setTextSize(mCurrentTextSize);

			// If we don't currently have text to draw, or the text size has changed, ellipsize...
			final CharSequence title = TextUtils.ellipsize(msText, mTextPaint, availableWidth, TextUtils.TruncateAt.END);
			if (!TextUtils.equals(title, mTextToDraw)) {
				mTextToDraw = title;
			}
		}
	}

	/**
	 * Re-calculate everything
	 *
	 * @author Melvin Lobo
	 */
	public void recalculate() {
		//Start calculations only after onLayout
		if (getHeight() > 0 && getWidth() > 0) {
			calculateBaseOffsets();
			calculateCurrentOffsets();
		}
	}

	/**
	 * Set the interpolated values
	 *
	 * @param textSize
	 *            The text size that we need to interpolate for
	 *
	 * @author Melvin Lobo
	 */
	private void setInterpolatedValues(final float textSize) {
		calculateUsingTextSize(textSize);
		ViewCompat.postInvalidateOnAnimation(this);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		final int saveCount = canvas.save();
		if (mTextToDraw != null && mbShouldDrawTitle) {
			float x = mCurrentDrawX;
			float y = mCurrentDrawY;
			// Update the TextPaint to the current text size
			mTextPaint.setTextSize(mCurrentTextSize);

			if (mScale != 1f) {
				canvas.scale(mScale, mScale, x, y);
			}

			//Draw the text or Spannable, depending on which has been initialized
			//canvas.drawText(mTextToDraw, 0, mTextToDraw.length(), x, y, mTextPaint);
			if(!mbDrawSpannable) {
				if(!mbIsMultifacetedString) {
					canvas.drawText(mTextToDraw, 0, mTextToDraw.length(), x, y, mTextPaint);
				}
				else {
					// Split the string
					String part1 = mTextToDraw.toString().substring(0, mnSplitStringAtPosition);
					String part2 = mTextToDraw.toString().substring(mnSplitStringAtPosition, mTextToDraw.length());
					// Draw first part
					canvas.drawText(part1, 0, part1.length(), x, y, mTextPaint);

					// Get the X Offset to move by
					Rect r = new Rect();
					mTextPaint.getTextBounds(part1, 0, part1.length(), r);
					float offset = r.width();

					// Use the new TextPaint with new typeFace
					TextPaint p2p = new TextPaint(mTextPaint);
					p2p.setTypeface(mSecondaryTypeface);
					canvas.drawText(part2, 0, part2.length(), x + offset, y, p2p);
				}
			}
			else {
				mStaticSpannableLayout = new StaticLayout(msDisplayText, mTextPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
				mStaticSpannableLayout.draw(canvas);
				mStaticMeasuringLayout = null;
			}
		}
		canvas.restoreToCount(saveCount);
	}

	/**
	 * Try and get the size for the expanded bounds. We need the height though, not only for the expanded font, but for vertical movement of the
	 * {@code mText}. We also keep a check for future reference so that we don't recalculate for the same width and height due to multiple measure
	 * passes. Creating a static layout repeatedly can be expensive. We're not using textBounds or measureText as we also need line spacing above and
	 * below the text
	 *
	 * @param widthMeasureSpec
	 *            The available width
	 * @param heightMeasureSpec
	 *            The available height
	 *
	 * @uthor Melvin Lobo
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// Get the height and width
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);

		if ((mnCalculatedWithWidth != width) || (mnCalculatedWithHeight != height)) {
			// Calculate the size required for the expanded size (If not the exact, then a precision will do)
			mfExpandedTextSize = getSingleLineTextSize(msDisplayText.toString(), mTextPaint, width, mfCollapsedTextSize, mfMaxExpandedTextSize, 0.5f,
					getResources().getDisplayMetrics());

			ensureStaticLayout(width, true);

			int fontHeightExpanded = mStaticMeasuringLayout.getHeight();
			int fontWidthExpanded = mStaticMeasuringLayout.getWidth();

			mnCalculatedWithWidth = getOptimalValue(fontWidthExpanded, width, MeasureSpec.getMode(widthMeasureSpec));
			mnCalculatedWithHeight = getOptimalValue(fontHeightExpanded, height, MeasureSpec.getMode(heightMeasureSpec));
		}
		setMeasuredDimension(mnCalculatedWithWidth, mnCalculatedWithHeight);
	}

	/**
	 * Get the optimum desired values based on the Mode recommendation during onMeasure
	 *
	 * @param nDesiredValue
	 *            The desired value that we calculated
	 * @param nRecommendedValue
	 *            The recommended value that is passed in an onMeasure Pass
	 * @param nMode
	 *            The Mode that is passed in onMeasure Pass
	 *
	 * @return The optimal value to be used based on the Mode
	 */
	private int getOptimalValue(int nDesiredValue, int nRecommendedValue, int nMode) {
		int nFinalWidth = nDesiredValue;

		switch (nMode) {
			case MeasureSpec.EXACTLY:
				nFinalWidth = nRecommendedValue; // No Choice
				break;
			case MeasureSpec.AT_MOST:
				nFinalWidth = Math.min(nDesiredValue, nRecommendedValue);
				break;
			default: // MeasureSpec.UNSPECIFIED
				nFinalWidth = nDesiredValue;
				break;
		}

		return nFinalWidth;
	}

	/**
	 * Ensure that the Static layout is created with the given value
	 *
	 * @param width
	 *            The width constraint
	 * @param bUseExpandedSize
	 *            True if we want to use the expanded size in the mTextPaint, false otherwise
	 *
	 * @author Melvin Lobo
	 */
	private void ensureStaticLayout(int width, boolean bUseExpandedSize) {
		if (mStaticMeasuringLayout != null)
			mStaticMeasuringLayout = null;

		mTextPaint.setTextSize((bUseExpandedSize) ? mfExpandedTextSize : mfCollapsedTextSize);
		mStaticMeasuringLayout = new StaticLayout(msDisplayText, mTextPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
	}

    /**
     * Calculate the expanded and collapsed bounds and set them. Note that we don't consider the values
     * passed as they are relative to the parent and contain margins, etc. For the bound calculations,
     * we always consider this layout and the top/left will start from (0, 0).
     *
     * @param changed
     *      If the layout has changed
     * @param left
     *      The left edge relative to the parent
     * @param top
     *      The top edge relative to the parent
     * @param right
     *      The right edge relative to the parent
     * @param bottom
     *      The bottom edge relative to the parent
     *
     * @author Melvin Lobo
     */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// If the expanded size is zero, recalculate
		if (mfExpandedTextSize == 0.0f) {
			// Expanded bounds: We already calculated it. We keep a bottom gravity
			// Calculate the size required for the expanded size (If not the exact, then a precision will do)
			mfExpandedTextSize = getSingleLineTextSize(msDisplayText.toString(), mTextPaint, getMeasuredWidth(), mfCollapsedTextSize,
					mfMaxExpandedTextSize, 0.5f, getResources().getDisplayMetrics());
		}

		// Collapsed bounds: Create the static layout with the collapsed text size and use the calculated
        // values to create the collapsed bounds
		ensureStaticLayout(getMeasuredWidth(), false);

		//Extract the padding out of the equation
		int nStaticHeight = mStaticMeasuringLayout.getHeight() - mStaticMeasuringLayout.getTopPadding() - getPaddingBottom();
		int nStaticWidth = mStaticMeasuringLayout.getWidth();
		setCollapsedBounds(0, 0, nStaticWidth, nStaticHeight);

		// Expanded Bounds: Use the same logic as above but with expanded text size
		ensureStaticLayout(getMeasuredWidth(), true);

		//Extract the padding out of the equation
		nStaticHeight = mStaticMeasuringLayout.getHeight() - mStaticMeasuringLayout.getTopPadding() - getPaddingBottom();
		nStaticWidth = mStaticMeasuringLayout.getWidth();
		setExpandedBounds(0, 0, nStaticWidth, nStaticHeight);

		recalculate();

		mStaticMeasuringLayout = null;
	}

	/**
	 * Convert dip to pixels
	 *
	 * param size The size to be converted
	 *
	 * @author Melvin Lobo
	 */
	private float d2x(int size) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getContext().getResources().getDisplayMetrics());
	}

	/**
	 * Getters and setters
	 *
	 */
	public Typeface getTypeface() {
		return mTextPaint.getTypeface();
	}

	public void setTypeface(Typeface typeface) {
		if (typeface == null) {
			typeface = Typeface.DEFAULT;
		}
		if (mTextPaint.getTypeface() != typeface) {
			mTextPaint.setTypeface(typeface);
			recalculate();
		}
	}

	public float getCollapsedTextSize() {
		return mfCollapsedTextSize;
	}

	public void setCollapsedTextSize(float textSize) {
		if (mfCollapsedTextSize != textSize) {
			mfCollapsedTextSize = textSize;
			recalculate();
		}
	}

	public float getExpandedTextSize() {
		return mfExpandedTextSize;
	}

	public void setExpandedTextSize(float textSize) {
		if (mfExpandedTextSize != textSize) {
			mfExpandedTextSize = textSize;
			recalculate();
		}
	}

	public int getExpandedTextColor() {
		return mExpandedTextColor;
	}

	public void setExpandedTextColor(int textColor) {
		if (mExpandedTextColor != textColor) {
			mExpandedTextColor = textColor;
			recalculate();
		}
	}

	public int getCollapsedTextColor() {
		return mCollapsedTextColor;
	}

	public void setCollapsedTextColor(int textColor) {
		if (mCollapsedTextColor != textColor) {
			mCollapsedTextColor = textColor;
			recalculate();
		}
	}

	public void setSpan(Spannable spannable) {
		mbDrawSpannable = true;
		msDisplayText.setSpan(spannable, 0, msDisplayText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
	}

	public void clearSpan() {
		mbDrawSpannable = false;
		msDisplayText = null;
		msDisplayText = new SpannableStringBuilder(msText);
	}

}
