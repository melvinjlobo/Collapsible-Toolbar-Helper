package abysmel.com.collapsibletoolbarhelper.widgets;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.design.widget.AppBarLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import abysmel.com.collapsibletoolbarhelper.R;
import abysmel.com.collapsibletoolbarhelper.helpers.ViewOffsetHelper;


/**
 * Created by Melvin Lobo on 11/13/2015.
 *
 * Highly adapted from the Android Open Source CollapsingToolbarLayout. The control is designed to work as a
 * direct child of AppBarLayout to benefit from its OffsetChangedListener events
 */
public class CollapsibleToolbarHelper extends android.support.percent.PercentRelativeLayout {
	//////////////////////////////////// CLASS MEMBERS /////////////////////////////////////////////
	/**
	 * Static Definitions
	 */
	private static final int    ALPHA_ANIMATION_DURATION           = 600;
	private static final int    DEFAULT_PADDING_SPACE_FOR_COLLAPSE = 10;
	private static final String SHAPE_1_COLOR                      = "#10000000";
	private static final String SHAPE_2_COLOR                      = "#13000000";
	private static final String SHAPE_3_COLOR                      = "#20000000";

	/**
	 * Store the inset based on the fitSystemWindows value. Although we do not draw a status bar scrim
	 * (Will get to it as an update feature later)
	 */
	private WindowInsetsCompat mSystemInsets;

	/**
	 * The OnOffsetChangedListener to be passed on to AppBarLayout. We rely heavily on this listener
	 * to make transitional changes to the children based on events sent by AppBarLayout
	 */
	private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

	/**
	 * The minimum height that the control will collapse to. This is ideally the bottom of the
	 * largest child that is NOT hiding along with the top and bottom padding
	 */
	private float mfMinimumCollapsibleHeight = 0.0f;

	/**
	 * Store the AppBar Layout drawable to add an "Elevation" in pre lollipop devices
	 * We place the background on a layer drawable to show an elevation. If the toolbar
	 * expands, we replace the original drawable back
	 */
	private Drawable mAppBarDrawable = null;

	//////////////////////////////////// CLASS METHODS /////////////////////////////////////////////

	/**
	 * Constructor to inflate the custom widget. The Android system calls the appropriate constructor
	 *
	 * @param context
	 * 		The context of the activity which acts as a parent to the widget
	 *
	 * @author Melvin Lobo
	 */
	public
	CollapsibleToolbarHelper(Context context) {
		this(context, null);
	}

	/**
	 * Constructor to inflate the custom widget. The Android system calls the appropriate constructor
	 *
	 * @param context
	 * 		The context of the activity which acts as a parent to the widget
	 * @param attrs
	 * 		The custom attributes associated with this widget and defined in the xml
	 *
	 * @author Melvin Lobo
	 */
	public
	CollapsibleToolbarHelper(Context context, AttributeSet attrs) {
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
	public
	CollapsibleToolbarHelper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}


	/**
	 * Function to initialize the views.
	 *
	 * @param context
	 *            The context of the activity which acts as a parent to the widget
	 * @param attrs
	 *            The custom attributes associated with this widget and defined in the xml
	 *
	 * @author Melvin Lobo
	 */
	private
	void init(Context context, AttributeSet attrs) {

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CollapsibleToolbarHelper, 0, 0);

			//Get the min height
			mfMinimumCollapsibleHeight = a.getDimension(R.styleable.CollapsibleToolbarHelper_minCollapseHeight, 0.0f);

			a.recycle();
		}

		// Notify the parent that we will be handling the onDraw function
		setWillNotDraw(false);

		// Store the insets if the hierarchy has a fitSystemWindows
		ViewCompat.setOnApplyWindowInsetsListener(this, new android.support.v4.view.OnApplyWindowInsetsListener() {

			@Override
			public
			WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
				mSystemInsets = insets;
				requestLayout();
				return insets.consumeSystemWindowInsets();
			}
		});
	}

	/**
	 * On Attached to window. Get the listener
	 *
	 * @author Melvin Lobo
	 */
	@Override
	protected
	void onAttachedToWindow() {
		super.onAttachedToWindow();

		final ViewParent parent = getParent();
		if (parent instanceof AppBarLayout) {
			if (mOnOffsetChangedListener == null) {
				mOnOffsetChangedListener = new OffsetChangedListener();
			}
			((AppBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);

			//Store the background to show an elevation on pre lollipop devices
			mAppBarDrawable = ((AppBarLayout) parent).getBackground();
		}
	}

	/**
	 * On Detached from window. Remove the listener
	 *
	 * @author Melvin Lobo
	 */
	@Override
	protected
	void onDetachedFromWindow() {
		// Remove our OnOffsetChangedListener if possible and it exists
		final ViewParent parent = getParent();
		if (mOnOffsetChangedListener != null && parent instanceof AppBarLayout) {
			((AppBarLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    /**
     * Layout the children by taking care that their rects do not overlap the parent insets if
     * fistSystemWindows is true
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int nBiggestChildHeight = 0;
        View biggestChild = null;
        for (int nCtr = 0, count = getChildCount(); nCtr < count; nCtr++) {
            final View child = getChildAt(nCtr);

            //If there is an inset, offset the child down to adjust accordingly
            if (mSystemInsets != null && !ViewCompat.getFitsSystemWindows(child)) {
                final int systemWindowInsetTop = mSystemInsets.getSystemWindowInsetTop();
                if (child.getTop() < systemWindowInsetTop) {
                    child.offsetTopAndBottom(systemWindowInsetTop);
                }
            }

            //Calculate only once, if the user has not provided the minimum height
            if(mfMinimumCollapsibleHeight == 0) {
                // Check for the child with the biggest height which is not hiding on scroll. We want to collapse to that height
                // We exclude CollapsingText if it's present as it can collapse to a smaller height
                // which would give a false representation now
                CollapsibleToolbarHelper.LayoutParams params = (LayoutParams) child.getLayoutParams();

                if ((params.getCollapseMode() != LayoutParams.PARALLAX_ON_SCROLL) &&
                        (params.getCollapseMode() != LayoutParams.HIDE_ON_COLLAPSE) &&
                        (child.getMeasuredHeight() > nBiggestChildHeight) &&
                        !(child instanceof CollapsibleTextLayout)) {
                    nBiggestChildHeight = child.getMeasuredHeight();
                    biggestChild = child;
                }
            }

            getViewOffsetHelper(child).onViewLayout();
        }

        // Calculate the minimum height by adding the margins of the largest child if the user has not provided
        // us with one
        if(mfMinimumCollapsibleHeight == 0) {
            int margins = 0;
            if(biggestChild != null) {
                CollapsibleToolbarHelper.LayoutParams params = (LayoutParams) biggestChild.getLayoutParams();
                margins += params.topMargin + params.bottomMargin;
            }
            mfMinimumCollapsibleHeight += nBiggestChildHeight + margins;        //Set the minimum height
        }

        // Set the minimum height so that the AppBarLayout does not collapse beyond that
        setMinimumHeight((int)Math.ceil(mfMinimumCollapsibleHeight));
    }


    /**
     * Get the trigger height for hiding / showing views. Build a minimum space around the min height
     * so that the views don't look cramped. (ToDo - get this value from the user)
     *
     * @author Melvin lobo
     */
    final private int getShowHideTriggerHeight() {
        return Math.round(mfMinimumCollapsibleHeight + getPaddingTop() + (d2x(DEFAULT_PADDING_SPACE_FOR_COLLAPSE) * 2));
    }

    ///////////////////////////////////// INNER CLASSES ////////////////////////////////////////////

    /**
     * Use the View Offset Helper to help with the View translations.
     * Check{@link abysmel.com.collapsibletoolbarhelper.helpers.ViewOffsetHelper}. Reference AOSP.
     *
     * @param view
     *      The View whose Offset has to be changed
     *
     * @return
     */
    private static ViewOffsetHelper getViewOffsetHelper(View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(android.support.design.R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(android.support.design.R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    /**
     * An inner class which implements the AppBarLayout.OnOffsetChangedListener. Movement events are
     * sent to this listener so that transitional changes can be made
     *
     * @author Melvin Lobo
     */
    private class OffsetChangedListener implements AppBarLayout.OnOffsetChangedListener {
        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {

            //Set the inset according to fitSystemWindows flag
            final int insetTop = (mSystemInsets != null) ? mSystemInsets.getSystemWindowInsetTop() : 0;

            //Get the scroll range of the this control
            final int scrollRange = layout.getTotalScrollRange();

            //Modify the offset and alpha of the child based on the current scroll value
            for (int nCtr = 0, count = getChildCount(); nCtr < count; nCtr++) {
                final View child = getChildAt(nCtr);
                final LayoutParams params = (LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (params.mnCollapseMode) {
                    case LayoutParams.MOVE_ON_SCROLL:

                        /*
                         * The idea is to avoid movement till the size of the collapse reached the bottom
                         * of children with these collapse mode options. Once it hits their bottom, these
                         * children will move up / down to / from the min height of the collapse
                         */
                        if (getHeight() - insetTop + verticalOffset >= child.getHeight()) {
                            offsetHelper.setTopAndBottomOffset(-verticalOffset);
                        }

                        break;
                    case LayoutParams.PARALLAX_ON_SCROLL:

                        /* Move the child with respect to the parallax multiplier only if visible. Note that we offset this child even
                         * if the alpha is zero because we need it to be in the right position when the control expands it self
                         * and all child views are visible
                         */
                        offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * LayoutParams.DEFAULT_PARALLAX_MULTIPLIER));

                        /* If the height of this control crosses the threshold trigger value, show / hide the
                         * child if it has a PARALLAX_ON_SCROLL flag set
                         */
                        if((getHeight() + verticalOffset) < (getShowHideTriggerHeight() + insetTop)) {
                            startAlphaAnimation(child, View.GONE);
                        }
                        else {
                            startAlphaAnimation(child, View.VISIBLE);
                        }
                        break;
                    case LayoutParams.PIN_ON_SCROLL:

                        /*
                         * Keep offsetting the view in the opposite direction of the movement so that
                         * it looks pinned
                         */
                        offsetHelper.setTopAndBottomOffset(-verticalOffset);
                        break;
                    case LayoutParams.HIDE_ON_COLLAPSE:

                        /*
                         * Show / Hide the control if the min trigger has been set
                         */
                        if((getHeight() + verticalOffset) < (getShowHideTriggerHeight() + insetTop)) {
                            startAlphaAnimation(child, View.GONE);
                        }
                        else {
                            startAlphaAnimation(child, View.VISIBLE);
                        }

                        //Also, keep negating the offset
                        offsetHelper.setTopAndBottomOffset(-verticalOffset);
                        break;
					case LayoutParams.SHOW_ON_COLLAPSE:

                        /*
                         * Show / Hide the control if the min trigger has been set
                         */
						if((getHeight() + verticalOffset) < (getShowHideTriggerHeight() + insetTop)) {
							startAlphaAnimation(child, View.VISIBLE);
						}
						else {
							startAlphaAnimation(child, View.GONE);
						}

						//Also, keep negating the offset
						offsetHelper.setTopAndBottomOffset(-verticalOffset);
						break;
                }

                //If the child is a collapsing text, then set the fraction as a function of the scroll offset
				if (child instanceof CollapsibleTextLayout) {
					final int expandRange = getHeight() - ViewCompat.getMinimumHeight(CollapsibleToolbarHelper.this) - insetTop;
					((CollapsibleTextLayout) child).setScrollOffsetFraction(Math.abs(verticalOffset) / (float) expandRange);
				}
            }

            /*
             * Set the elevation on the AppBar so that the content can scroll below it
             */
            if (Math.abs(verticalOffset) == scrollRange) {
                // If we have some pinned children, and we're offset to only show those views,
                // we want to be elevate
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    ViewCompat.setElevation(layout, layout.getTargetElevation());
                else
	                setBackgroundResource(layout, mAppBarDrawable, true);
            }
            else {
                // Otherwise, we're inline with the content
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    ViewCompat.setElevation(layout, 0f);
                else
	                setBackgroundResource(layout, mAppBarDrawable, false);
            }
        }
    }

    /**
     * Convert dip to pixels
     *
     * param size
     *            The size to be converted
     *
     * @author Melvin Lobo
     */
    private float d2x(int size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources().getDisplayMetrics());
    }


    /**
     * Start the alpha animation on a view.
     *
     * @param view
     *      The view on which the alpha animation is to be done
     * @param visibility
     *      The visibility of the view, based on which, the alpha will be set
     *
     *  @author Melvin Lobo
     */
    private void startAlphaAnimation(final View view, final int visibility) {
        //return if we have already met our objective
        if((visibility == View.GONE && (view.getAlpha() == 0.0f)) || (visibility == View.VISIBLE && (view.getAlpha() == 1.0f))) {
            return;
        }
        else {
			ViewCompat.setAlpha(view,(visibility == View.GONE) ? 1.0f : 0.0f);
			ViewCompat.animate(view)
					  .alpha((visibility == View.GONE) ? 0.0f : 1.0f)
					  .setDuration(ALPHA_ANIMATION_DURATION)
					  .setInterpolator(new FastOutSlowInInterpolator());
        }
    }

	/**
	 * Set the background drawable
	 *
	 * @param layout
	 *      The layout whose background needs to be set
	 * @param backgroundDrawable
	 *      The background drawable
	 * @param bCreateBackground
	 *      Create the background, else, just assign the drawable
	 *
	 * @author Melvin Lobo
	 */
	private void setBackgroundResource(View layout, Drawable backgroundDrawable, boolean bCreateBackground) {

        /*
         * A very nasty bug that exists pre-kitkat which I spent hours figuring out. If we assign a
         * Layerdrawable or a 9-patch, the view loses its padding (weird!). It works for a png or a shape
         * as expected. So, we need to get the padding, set the background resource and re-set the padding
         * back to the original values we retrieved. This should give the background shapre to pre-lollipop devices
         */
		int pL = layout.getPaddingLeft();
		int pR = layout.getPaddingRight();
		int pT = layout.getPaddingTop();
		int pB = layout.getPaddingBottom();

		layout.setBackground((bCreateBackground) ? createElevatedBackgroundShape(backgroundDrawable) : backgroundDrawable);

		//Re-set the padding back again
		layout.setPadding(pL, pT, pR, pB);
	}

    /**
     * Create the background shape / Layer -List programmatically. TO have the shadow effect, we need
     * shapes placed on top of the other with varying Alphas and with an inset, to give an illusion of elevation
     * An xml can also be provided to do this statically
     * The shape stack (starting from bottom) is: Shape1, Shape2, Shape3, Shape4, foreground
     *
     * @author Melvin Lobo
     */
    private Drawable createElevatedBackgroundShape(Drawable foreground) {
        Drawable backgroundDrawable = null;

	    //Get our own version of the foreground in case things go south
	    if(foreground == null) {
		    //Foreground Shape
		    RectShape foregroundRect = new RectShape();
		    ShapeDrawable foregroundShape = new ShapeDrawable(foregroundRect);
		    foregroundShape.getPaint().setColor(Color.TRANSPARENT);
		    foreground = foregroundShape;
	    }

        //First Shape
        RectShape rect1 = new RectShape();
        ShapeDrawable shape1 = new ShapeDrawable(rect1);
        shape1.getPaint().setColor(Color.parseColor(SHAPE_1_COLOR));
        shape1.invalidateSelf();

        //Second Shape
        RectShape rect2 = new RectShape();
        ShapeDrawable shape2 = new ShapeDrawable(rect2);
        shape2.getPaint().setColor(Color.parseColor(SHAPE_2_COLOR));
        shape2.invalidateSelf();

        //Third Shape
        RectShape rect3 = new RectShape();
        ShapeDrawable shape3 = new ShapeDrawable(rect3);
        shape3.getPaint().setColor(Color.parseColor(SHAPE_3_COLOR));
        shape3.invalidateSelf();

        //Fourth Shape
        RectShape rect4 = new RectShape();
        ShapeDrawable shape4 = new ShapeDrawable(rect4);
        shape4.getPaint().setColor(Color.parseColor(SHAPE_2_COLOR));
        shape4.invalidateSelf();

        //Create an array of shapes for the layer list
        Drawable[] layerArray = {shape1, shape2, shape3, shape4, foreground};

        LayerDrawable drawable = new LayerDrawable(layerArray);

        //Set the insets to get the elevated shadow effect
        drawable.setLayerInset(0, 0, 0, 0, 0);
        drawable.setLayerInset(1, 0, 0, 0, (int) d2x(1));
        drawable.setLayerInset(2, 0, 0, 0, (int) d2x(2));
        drawable.setLayerInset(3, 0, 0, 0, (int) d2x(3));
        drawable.setLayerInset(4, 0, 0, 0, (int) d2x(4));

        backgroundDrawable = drawable;

        return backgroundDrawable;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public PercentRelativeLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected PercentRelativeLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    ////////////////////////////////////////// INNER CLASSES ///////////////////////////////////////
    /**
     * Defining custom attributes that the children can have as to whether they want to fade out /
     * fade in.
     *
     * @author Melvin Lobo
     */
    public static class LayoutParams extends PercentRelativeLayout.LayoutParams {

        ////////////////////////////////////// CLASS MEMBERS ///////////////////////////////////////
        /**
         * The static members
         */
        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.7f;

        /**
         * Define the integers
         *
         * For simplicity from Android documentation -
         * We create a new annotation (that's what
         * the above line "public @interface CollapseMode {}" does) and then we annotate the
         * annotation itself with @IntDef, and we specify the constants that are the valid constants
         * for return values or parameters. We also add the line "@Retention(RetentionPolicy.SOURCE)"
         * to tell the compiler that usages of the new typedef annotation do not need to be recorded
         * in .class files.
         */
        @IntDef({
                PARALLAX_ON_SCROLL,
                MOVE_ON_SCROLL,
                PIN_ON_SCROLL,
                HIDE_ON_COLLAPSE,
				SHOW_ON_COLLAPSE
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {}

        /**
         * The view will parallax and then eventually fade out when the min height trigger is set
         * {@link CollapsibleToolbarHelper}.
         */
        public static final int PARALLAX_ON_SCROLL = 0;

        /**
         * The view will move on scroll till the min height and then stay there
         * {@link CollapsibleToolbarHelper}.
         */
        public static final int MOVE_ON_SCROLL = 1;

        /**
         * The view will pin to its position
         * {@link CollapsibleToolbarHelper}.
         */
        public static final int PIN_ON_SCROLL = 2;

        /**
         * The view will pin to its position and hide after the layout reaches its min height
         * {@link CollapsibleToolbarHelper}.
         */
        public static final int HIDE_ON_COLLAPSE = 3;

        /**
         * The view will show after the layout reaches its min height / collapses
         * {@link CollapsibleToolbarHelper}.
         */
        public static final int SHOW_ON_COLLAPSE = 4;

        /**
         * The current collapse mode
         */
        private int mnCollapseMode = PIN_ON_SCROLL;

        /**
         * The parallax multipler if applicable
         */
        private float mfParallaxMultiplier = DEFAULT_PARALLAX_MULTIPLIER;

        ////////////////////////////////////// CLASS METHODS ///////////////////////////////////////

        /**
         * Constructor
         * @param width
         *      The width of the View
         * @param height
         *      The height of the view
         *
         * @author Melvin Lobo
         */
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        /**
         * Constructor
         * @param p
         *      The Layout param with teh values
         *
         * @author Melvin Lobo
         */
        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        /**
         * Constructor
         * @param source
         *      The Layout param with the values
         *
         * @author Melvin Lobo
         */
        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        /**
         * Constructor
         * @param source
         *      The Layout param with the values
         *
         * @author Melvin Lobo
         */
        public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }

        /**
         * Constructor
         * @param c
         *      The context
         * @param attrs
         *      The attribute set
         *
         * @author Melvin Lobo
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs,
                    R.styleable.CollapsibleToolbarHelper);
            mnCollapseMode = a.getInt(R.styleable.CollapsibleToolbarHelper_collapseMode,
                    PIN_ON_SCROLL);
            a.recycle();
        }

        /**
         * Set the collapse mode.
         *
         * @param collapseMode one of {@link #MOVE_ON_SCROLL} or {@link #PARALLAX_ON_SCROLL}
         */
        public void setCollapseMode(@CollapseMode int collapseMode) {
            mnCollapseMode = collapseMode;
        }

        /**
         * Returns the requested collapse mode.
         *
         * @return the current mode. One of {@link #MOVE_ON_SCROLL} or {@link #PARALLAX_ON_SCROLL}
         */
        @CollapseMode
        public int getCollapseMode() {
            return mnCollapseMode;
        }

        /**
         * Set the parallax scroll multiplier used in conjunction with
         * {@link #PARALLAX_ON_SCROLL}. A value of {@code 0.0} indicates no parallax movement at all,
         * {@code 1.0f} indicates normal scroll movement. This is along with the alpha change.
         *
         * @param multiplier the multiplier.
         */
        public void setParallaxMultiplier(float multiplier) {
            mfParallaxMultiplier = multiplier;
        }

        /**
         * Returns the parallax scroll multiplier used in conjunction with
         * {@link #PARALLAX_ON_SCROLL}.
         *
         */
        public float getParallaxMultiplier() {
            return mfParallaxMultiplier;
        }
    }

}