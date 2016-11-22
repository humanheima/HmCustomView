package com.humanheima.hmcustomview.ui.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CompoundButton;

import com.humanheima.hmcustomview.R;
import com.humanheima.hmcustomview.util.ColorUtils;

/**
 * Created by dmw on 2016/11/21.
 */
public class SwitchButton extends CompoundButton {

    private String tag = "my_switch_button";
    //默认的thumb和整个switchButton的宽度的比例
    public static final float DEFAULT_BACK_MEASURE_RATIO = 1.8f;
    public static final int DEFAULT_THUMB_SIZE_DP = 20;
    public static final int DEFAULT_THUMB_MARGIN_DP = 2;
    public static final int DEFAULT_ANIMATION_DURATION = 250;
    public static final int DEFAULT_TEXT_MARGIN_DP = 2;
    //默认着色
    public static final int DEFAULT_TINT_COLOR = 0x327FC2;

    private static int[] CHECKED_PRESSED_STATE = new int[]{android.R.attr.state_checked, android.R.attr.state_enabled, android.R.attr.state_pressed};
    private static int[] UNCHECKED_PRESSED_STATE = new int[]{-android.R.attr.state_checked, android.R.attr.state_enabled, android.R.attr.state_pressed};
    private float mStartX, mStartY, mLastX;

    private ColorStateList mBackColor, mThumbColor;
    // whether using Drawable for thumb or back
    private boolean mIsThumbUseDrawable, mIsBackUseDrawable;
    private Paint mPaint;

    private PointF mThumbSizeF;
    private float mBackMeasureRatio;
    private RectF mThumbMargin, mBackRectF, mThumbRectF, mSafeRectF;
    private int mTintColor;
    private int mCurrThumbColor, mCurrBackColor, mNextBackColor;
    private Drawable mThumbDrawable, mBackDrawable;
    private boolean mFadeBack = true;
    private boolean mAutoAdjustTextPosition = true;
    private int mTouchSlop;
    private int mClickTimeout;
    private float mThumbRadius, mBackRadius;
    // temp position of thumb when dragging or animating
    private RectF mPresentThumbRectF;

    /**
     * switch button 文字相关
     */
    //绘制文字的画笔
    private TextPaint mTextPaint;
    private float mTextWidth, mTextHeight;
    private int mOnTextColor, mOffTextColor;
    private CharSequence mTextOn, mTextOff;
    private Layout mOnLayout;
    private Layout mOffLayout;
    private float mTextMarginH;
    private RectF mTextOnRectF, mTextOffRectF;
    /**
     * 动画相关
     */
    private ObjectAnimator mProcessAnimator;
    private float process;
    private long mAnimationDuration;
    private Drawable mCurrentBackDrawable, mNextBackDrawable;

    public SwitchButton(Context context) {
        super(context);
        init(null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mClickTimeout = ViewConfiguration.getPressedStateDuration() + ViewConfiguration.getTapTimeout();
        mProcessAnimator = ObjectAnimator.ofFloat(this, "process", 0, 0).setDuration(DEFAULT_ANIMATION_DURATION);
        mProcessAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mTextPaint = getPaint();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbSizeF = new PointF();
        mThumbMargin = new RectF();
        mBackRectF = new RectF();
        mSafeRectF = new RectF();
        mThumbRectF = new RectF();
        mPresentThumbRectF = new RectF();
        mTextOnRectF = new RectF();
        mTextOffRectF = new RectF();
        Resources res = getResources();
        float density = res.getDisplayMetrics().density;
        float margin = density * DEFAULT_THUMB_MARGIN_DP;
        float marginLeft = 0;
        float marginRight = 0;
        float marginTop = 0;
        float marginBottom = 0;
        int tintColor = 0;
        String textOn = null;
        String textOff = null;
        ColorStateList backColor = null;
        float textMarginH = density * DEFAULT_TEXT_MARGIN_DP;
        float thumbWidth = density * DEFAULT_THUMB_SIZE_DP;
        float thumbHeight = density * DEFAULT_THUMB_SIZE_DP;
        float thumbRadius = density * DEFAULT_THUMB_SIZE_DP / 2;
        float backRadius = thumbRadius;
        float backMeasureRatio = DEFAULT_BACK_MEASURE_RATIO;
        int animationDuration = DEFAULT_ANIMATION_DURATION;
        boolean autoAdjustTextPosition = true;
        TypedArray ta = attrs == null ? null : getContext().obtainStyledAttributes(attrs, R.styleable.MySwitchButton);
        if (ta != null) {
            mThumbDrawable = ta.getDrawable(R.styleable.MySwitchButton_thumbDrawable);
            mBackDrawable = ta.getDrawable(R.styleable.MySwitchButton_backDrawable);
            mThumbColor = ta.getColorStateList(R.styleable.MySwitchButton_thumbColor);
            backColor = ta.getColorStateList(R.styleable.MySwitchButton_backColor);
            marginLeft = ta.getDimension(R.styleable.MySwitchButton_thumbMarginLeft, margin);
            marginRight = ta.getDimension(R.styleable.MySwitchButton_thumbMarginRight, margin);
            marginTop = ta.getDimension(R.styleable.MySwitchButton_thumbMarginTop, margin);
            marginBottom = ta.getDimension(R.styleable.MySwitchButton_thumbMarginBottom, margin);
            thumbRadius = ta.getDimension(R.styleable.MySwitchButton_thumbRadius, Math.min(thumbWidth, thumbHeight) / 2.f);
            backRadius = ta.getDimension(R.styleable.MySwitchButton_backRadius, thumbRadius + density * 2f);
            backMeasureRatio = ta.getFloat(R.styleable.MySwitchButton_backMeasureRatio, backMeasureRatio);
            tintColor = ta.getColor(R.styleable.MySwitchButton_tintColor, tintColor);
            animationDuration = ta.getInteger(R.styleable.MySwitchButton_animationDuration, DEFAULT_ANIMATION_DURATION);
            textOn = ta.getString(R.styleable.MySwitchButton_textOn);
            textOff = ta.getString(R.styleable.MySwitchButton_textOff);
            textMarginH = Math.max(textMarginH, backRadius / 2);
            textMarginH = ta.getDimension(R.styleable.MySwitchButton_textMarginH, textMarginH);
            autoAdjustTextPosition = ta.getBoolean(R.styleable.MySwitchButton_autoAdjustTextPosition, autoAdjustTextPosition);
            ta.recycle();
        }
        // click
        ta = attrs == null ? null : getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.focusable, android.R.attr.clickable});
        if (ta != null) {
            boolean focusable = ta.getBoolean(0, true);
            //noinspection ResourceType
            boolean clickable = ta.getBoolean(1, focusable);
            setFocusable(focusable);
            setClickable(clickable);
            ta.recycle();
        }
        mIsThumbUseDrawable = mThumbDrawable != null;
        mIsBackUseDrawable = mBackDrawable != null;
        mTextOn = textOn;
        mTextOff = textOff;
        mTextMarginH = textMarginH;
        mAutoAdjustTextPosition = autoAdjustTextPosition;
        mAnimationDuration = animationDuration;
        mProcessAnimator.setDuration(mAnimationDuration);
        mThumbSizeF.set(thumbWidth, thumbHeight);
        // size & measure params must larger than 1
        mBackMeasureRatio = mThumbMargin.width() >= 0 ? Math.max(backMeasureRatio, 1) : backMeasureRatio;
        mTintColor = tintColor;
        mThumbRadius = thumbRadius;
        mBackRadius = backRadius;
        mBackColor = backColor;
        // margin
        mThumbMargin.set(marginLeft, marginTop, marginRight, marginBottom);
        if (mTintColor == 0) {
            TypedValue typedValue = new TypedValue();
            boolean found = getContext().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
            mTintColor = found ? typedValue.data : DEFAULT_TINT_COLOR;
        }
        if (!mIsThumbUseDrawable && mThumbColor == null) {
            mThumbColor = ColorUtils.generateThumbColorWithTintColor(mTintColor);
            mCurrThumbColor = mThumbColor.getDefaultColor();
            Log.e(tag, "mCurrThumbColor=" + mCurrThumbColor);
        }
        if (!mIsBackUseDrawable && mBackColor == null) {
            mBackColor = ColorUtils.generateBackColorWithTintColor(mTintColor);
            mCurrBackColor = mBackColor.getDefaultColor();
            mNextBackColor = mBackColor.getColorForState(CHECKED_PRESSED_STATE, mCurrBackColor);
        }

        if (isChecked()) {
            setProcess(1);
        }
    }

    private Layout makeLayout(CharSequence text) {
        return new StaticLayout(text, mTextPaint, (int) Math.ceil(Layout.getDesiredWidth(text, mTextPaint)), Layout.Alignment.ALIGN_CENTER, 1.f, 0, false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOnLayout == null && mTextOn != null) {
            mOnLayout = makeLayout(mTextOn);
        }
        if (mOffLayout == null && mTextOff != null) {
            mOffLayout = makeLayout(mTextOff);
        }
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measuredWidth;
        //取整
        int minWidth = ceil(mThumbSizeF.x * mBackMeasureRatio);
        if (mIsBackUseDrawable) {
            minWidth = Math.max(minWidth, mBackDrawable.getMinimumWidth());
        }
        float onWidth = mOnLayout != null ? mOnLayout.getWidth() : 0;
        float offWidth = mOffLayout != null ? mOffLayout.getWidth() : 0;
        if (onWidth != 0 || offWidth != 0) {
            mTextWidth = Math.max(onWidth, offWidth) + mTextMarginH * 2;
            float left = minWidth - mThumbSizeF.x;
            if (left < mTextWidth) {
                minWidth += mTextWidth - left;
            }
        } else {
            mTextWidth = 0;
        }
        minWidth = Math.max(minWidth, ceil(minWidth + mThumbMargin.left + mThumbMargin.right));
        minWidth = Math.max(minWidth, minWidth + getPaddingLeft() + getPaddingRight());
        minWidth = Math.max(minWidth, getSuggestedMinimumWidth());

        if (widthMode == MeasureSpec.EXACTLY) {
            measuredWidth = Math.max(minWidth, widthSize);
        } else {
            measuredWidth = minWidth;
            if (widthMode == MeasureSpec.AT_MOST) {
                measuredWidth = Math.min(measuredWidth, widthSize);
            }
        }
        return measuredWidth;
    }

    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int measuredHeight;
        int minHeight = ceil(Math.max(mThumbSizeF.y, mThumbSizeF.y + mThumbMargin.top + mThumbMargin.right));
        float onHeight = mOnLayout != null ? mOnLayout.getHeight() : 0;
        float offHeight = mOffLayout != null ? mOffLayout.getHeight() : 0;
        if (onHeight != 0 || offHeight != 0) {
            mTextHeight = Math.max(onHeight, offHeight);
            minHeight = ceil(Math.max(minHeight, mTextHeight));
        } else {
            mTextHeight = 0;
        }
        minHeight = Math.max(minHeight, getSuggestedMinimumHeight());
        minHeight = Math.max(minHeight, minHeight + getPaddingTop() + getPaddingBottom());
        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = Math.max(minHeight, heightSize);
        } else {
            measuredHeight = minHeight;
            if (heightMode == MeasureSpec.AT_MOST) {
                measuredHeight = Math.min(measuredHeight, heightSize);
            }
        }
        return measuredHeight;
    }

    private int ceil(double dimen) {
        return (int) Math.ceil(dimen);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsBackUseDrawable) {
            if (mFadeBack && mCurrentBackDrawable != null && mNextBackDrawable != null) {
                int alpha = (int) (255 * (isChecked() ? getProcess() : (1 - getProcess())));
                mCurrentBackDrawable.setAlpha(alpha);
                mCurrentBackDrawable.draw(canvas);
                alpha = 255 - alpha;
                mNextBackDrawable.setAlpha(alpha);
                mNextBackDrawable.draw(canvas);
            } else {
                mBackDrawable.setAlpha(255);
                mBackDrawable.draw(canvas);
            }
        } else {
            if (mFadeBack) {
                Log.e(tag, "mFadeBack ischecked==" + isChecked());
                int alpha;
                int colorAlpha;
                alpha = (int) (255 * (isChecked() ? getProcess() : (1 - getProcess())));

                colorAlpha = Color.alpha(mCurrBackColor);
                colorAlpha = colorAlpha * alpha / 255;
                mPaint.setARGB(colorAlpha, Color.red(mCurrBackColor), Color.green(mCurrBackColor), Color.blue(mCurrBackColor));
                canvas.drawRoundRect(mBackRectF, mBackRadius, mBackRadius, mPaint);

                // next back
                alpha = 255 - alpha;
                colorAlpha = Color.alpha(mNextBackColor);
                colorAlpha = colorAlpha * alpha / 255;
                mPaint.setARGB(colorAlpha, Color.red(mNextBackColor), Color.green(mNextBackColor), Color.blue(mNextBackColor));
                canvas.drawRoundRect(mBackRectF, mBackRadius, mBackRadius, mPaint);

                mPaint.setAlpha(255);
            } else {
                Log.e("my_switch_button", "mFadeBack==false");
                mPaint.setColor(mCurrBackColor);
                canvas.drawRoundRect(mBackRectF, mBackRadius, mBackRadius, mPaint);
            }
            // text
            Layout switchText = getProcess() > 0.5 ? mOnLayout : mOffLayout;
            RectF textRectF = getProcess() > 0.5 ? mTextOnRectF : mTextOffRectF;
            if (switchText != null && textRectF != null) {
                int alpha = (int) (255 * (getProcess() >= 0.75 ? getProcess() * 4 - 3 : (getProcess() < 0.25 ? 1 - getProcess() * 4 : 0)));
                int textColor = getProcess() > 0.5 ? mOnTextColor : mOffTextColor;
                int colorAlpha = Color.alpha(textColor);
                colorAlpha = colorAlpha * alpha / 255;
                switchText.getPaint().setARGB(colorAlpha, Color.red(textColor), Color.green(textColor), Color.blue(textColor));
                canvas.save();
                canvas.translate(textRectF.left, textRectF.top);
                switchText.draw(canvas);
                canvas.restore();
            }
        }
        // thumb
        mPresentThumbRectF.set(mThumbRectF);
        mPresentThumbRectF.offset(process * mSafeRectF.width(), 0);
        if (mIsThumbUseDrawable) {
            mThumbDrawable.setBounds((int) mPresentThumbRectF.left, (int) mPresentThumbRectF.top, ceil(mPresentThumbRectF.right), ceil(mPresentThumbRectF.bottom));
            mThumbDrawable.draw(canvas);
        } else {
            mPaint.setColor(mCurrThumbColor);
            canvas.drawRoundRect(mPresentThumbRectF, mThumbRadius, mThumbRadius, mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            setup();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (!mIsThumbUseDrawable && mThumbColor != null) {
            mCurrThumbColor = mThumbColor.getColorForState(getDrawableState(), mCurrThumbColor);
        } else {
            setDrawableState(mThumbDrawable);
        }
        int[] nextState = isChecked() ? UNCHECKED_PRESSED_STATE : CHECKED_PRESSED_STATE;
        ColorStateList textColors = getTextColors();
        if (textColors != null) {
            int defaultTextColor = textColors.getDefaultColor();
            mOnTextColor = textColors.getColorForState(CHECKED_PRESSED_STATE, defaultTextColor);
            mOffTextColor = textColors.getColorForState(UNCHECKED_PRESSED_STATE, defaultTextColor);
        }
        if (!mIsBackUseDrawable && mBackColor != null) {
            mCurrBackColor = mBackColor.getColorForState(getDrawableState(), mCurrBackColor);
            mNextBackColor = mBackColor.getColorForState(nextState, mCurrBackColor);
        } else {
            if (mBackDrawable instanceof StateListDrawable && mFadeBack) {
                mBackDrawable.setState(nextState);
                mNextBackDrawable = mBackDrawable.getCurrent().mutate();
            } else {
                mNextBackDrawable = null;
            }
            setDrawableState(mBackDrawable);
            if (mBackDrawable != null) {
                mCurrentBackDrawable = mBackDrawable.getCurrent().mutate();
            }
        }
    }

    private void setDrawableState(Drawable drawable) {
        if (drawable != null) {
            int[] myDrawableState = getDrawableState();
            drawable.setState(myDrawableState);
            invalidate();
        }
    }

    /**
     * set up the rect of back and thumb
     */
    private void setup() {
        float thumbTop = getPaddingTop() + Math.max(0, mThumbMargin.top);
        float thumbLeft = getPaddingLeft() + Math.max(0, mThumbMargin.left);
        if (mOnLayout != null && mOffLayout != null) {
            if (mThumbMargin.top + mThumbMargin.bottom > 0) {
                // back is higher than thumb
                float addition = (getMeasuredHeight() - getPaddingBottom() - getPaddingTop() - mThumbSizeF.y - mThumbMargin.top - mThumbMargin.bottom) / 2;
                thumbTop += addition;
            }
        }
        if (mIsThumbUseDrawable) {
            mThumbSizeF.x = Math.max(mThumbSizeF.x, mThumbDrawable.getMinimumWidth());
            mThumbSizeF.y = Math.max(mThumbSizeF.y, mThumbDrawable.getMinimumHeight());
        }
        mThumbRectF.set(thumbLeft, thumbTop, thumbLeft + mThumbSizeF.x, thumbTop + mThumbSizeF.y);

        float backLeft = mThumbRectF.left - mThumbMargin.left;
        float textDiffWidth = Math.min(0, (Math.max(mThumbSizeF.x * mBackMeasureRatio, mThumbSizeF.x + mTextWidth) - mThumbRectF.width() - mTextWidth) / 2);
        float textDiffHeight = Math.min(0, (mThumbRectF.height() + mThumbMargin.top + mThumbMargin.bottom - mTextHeight) / 2);
        mBackRectF.set(backLeft + textDiffWidth,
                mThumbRectF.top - mThumbMargin.top + textDiffHeight,
                backLeft + mThumbMargin.left + Math.max(mThumbSizeF.x * mBackMeasureRatio, mThumbSizeF.x + mTextWidth) + mThumbMargin.right - textDiffWidth,
                mThumbRectF.bottom + mThumbMargin.bottom - textDiffHeight);

        mSafeRectF.set(mThumbRectF.left, 0, mBackRectF.right - mThumbMargin.right - mThumbRectF.width(), 0);

        float minBackRadius = Math.min(mBackRectF.width(), mBackRectF.height()) / 2.f;
        mBackRadius = Math.min(minBackRadius, mBackRadius);

        if (mBackDrawable != null) {
            mBackDrawable.setBounds((int) mBackRectF.left, (int) mBackRectF.top, ceil(mBackRectF.right), ceil(mBackRectF.bottom));
        }
        if (mOnLayout != null) {
            float marginOnX = mBackRectF.left + (mBackRectF.width() - mThumbRectF.width() - mThumbMargin.right - mOnLayout.getWidth()) / 2 + (mThumbMargin.left < 0 ? mThumbMargin.left * -0.5f : 0);
            if (!mIsBackUseDrawable && mAutoAdjustTextPosition) {
                marginOnX += mBackRadius / 4;
            }
            float marginOnY = mBackRectF.top + (mBackRectF.height() - mOnLayout.getHeight()) / 2;
            mTextOnRectF.set(marginOnX, marginOnY, marginOnX + mOnLayout.getWidth(), marginOnY + mOnLayout.getHeight());
        }

        if (mOffLayout != null) {
            float marginOffX = mBackRectF.right - (mBackRectF.width() - mThumbRectF.width() - mThumbMargin.left - mOffLayout.getWidth()) / 2 - mOffLayout.getWidth() + (mThumbMargin.right < 0 ? mThumbMargin.right * 0.5f : 0);
            if (!mIsBackUseDrawable && mAutoAdjustTextPosition) {
                marginOffX -= mBackRadius / 4;
            }
            float marginOffY = mBackRectF.top + (mBackRectF.height() - mOffLayout.getHeight()) / 2;
            mTextOffRectF.set(marginOffX, marginOffY, marginOffX + mOffLayout.getWidth(), marginOffY + mOffLayout.getHeight());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !isClickable() || !isFocusable()) {
            Log.e(tag, "onTouchEvent !isEnabled() || !isClickable() || !isFocusable()");
            return false;
        }
        int action = event.getAction();
        float deltaX = event.getX() - mStartX;
        float deltaY = event.getY() - mStartY;
        // status the view going to change to when finger released
        boolean nextStatus;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ViewParent parent = getParent();
                if (parent != null) {
                    //不允许拦截事件
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                mStartX = event.getX();
                mStartY = event.getY();
                mLastX = mStartX;
                setPressed(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                Log.e(tag, getLeft() + "," + getRight() + "," + getTop() + "," + getBottom());
                int leftBoard = getLeft() - 8;
                int rightBoard = getRight() + 8;
                int topBoard = getTop() - 4;
                int bottomBoard = getBottom() + 4;
                /*if (x > leftBoard && x < rightBoard && y > topBoard && y < bottomBoard) {
                    setProcess(getProcess() + (x - mLastX) / mSafeRectF.width());
                }*/
                setProcess(getProcess() + (x - mLastX) / mSafeRectF.width());
                mLastX = x;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                setPressed(false);
                nextStatus = getProcess() > 0.5f;
                float time = event.getEventTime() - event.getDownTime();
                if (deltaX < mTouchSlop && deltaY < mTouchSlop && time < mClickTimeout) {
                    Log.e(tag, " performClick()");
                    performClick();
                } else {
                    if (nextStatus != isChecked()) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        setChecked(nextStatus);
                    } else {
                        animateToState(nextStatus);
                    }
                }
                break;

            default:
                break;
        }
        return true;

    }

    @Override
    public void setChecked(final boolean checked) {
        // animate before super.setChecked() become user may call setChecked again in OnCheckedChangedListener
        Log.e(tag, "setChecked checked==" + checked);
        if (isChecked() != checked) {

            animateToState(checked);
        }
        super.setChecked(checked);
    }

    /**
     * processing animation
     *
     * @param checked checked or unChecked
     */
    private void animateToState(boolean checked) {
        if (mProcessAnimator == null) {
            return;
        }
        if (mProcessAnimator.isRunning()) {
            mProcessAnimator.cancel();
        }
        mProcessAnimator.setDuration(mAnimationDuration);
        if (checked) {
            mProcessAnimator.setFloatValues(process, 1f);
        } else {
            mProcessAnimator.setFloatValues(process, 0);
        }
        mProcessAnimator.start();
    }

    /**
     * @param process
     */
    public final void setProcess(final float process) {
        float tp = process;
        if (tp > 1) {
            tp = 1;
        } else if (tp < 0) {
            tp = 0;
        }
        this.process = tp;
        invalidate();
    }

    public final float getProcess() {
        return process;
    }

    public void setCheckedImmediately(boolean checked) {
        super.setChecked(checked);
        if (mProcessAnimator != null && mProcessAnimator.isRunning()) {
            mProcessAnimator.cancel();
        }
        setProcess(checked ? 1 : 0);
        invalidate();
    }

    public void setText(CharSequence onText, CharSequence offText) {
        mTextOn = onText;
        mTextOff = offText;
        mOnLayout = null;
        mOffLayout = null;
        requestLayout();
        invalidate();
    }
}
