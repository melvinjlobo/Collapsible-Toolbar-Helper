package abysmel.com.collapsibletoolbarhelper.widgets;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * Created by Melvin Lobo on 11/25/2015.
 *
 * Reference -
 * http://stackoverflow.com/questions/4819049/how-can-i-use-typefacespan-or-stylespan-with-a-custom-typeface
 */
public class CustomTypefaceSpan extends MetricAffectingSpan
{
    ////////////////////////////////////////// CLASS MEMBERS ///////////////////////////////////////
    private final Typeface mTypeface;

    ////////////////////////////////////////// CLASS METHODS ///////////////////////////////////////
    public CustomTypefaceSpan(final Typeface typeface)
    {
        mTypeface = typeface;
    }

    @Override
    public void updateDrawState(final TextPaint drawState)
    {
        apply(drawState);
    }

    @Override
    public void updateMeasureState(final TextPaint paint)
    {
        apply(paint);
    }

    private void apply(final Paint paint)
    {
        final Typeface oldTypeface = paint.getTypeface();
        final int oldStyle = oldTypeface != null ? oldTypeface.getStyle() : 0;
        final int fakeStyle = oldStyle & ~mTypeface.getStyle();

        if ((fakeStyle & Typeface.BOLD) != 0)
        {
            paint.setFakeBoldText(true);
        }

        if ((fakeStyle & Typeface.ITALIC) != 0)
        {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(mTypeface);
    }
}
