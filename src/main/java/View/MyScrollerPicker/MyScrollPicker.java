package View.MyScrollerPicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sadam.sadamlibarary.R;

import java.util.List;

import View.MyScrollerPicker.util.ScreenUtil;

public class MyScrollPicker extends RecyclerView {

    /**
     * 以下是可以自定义的参数属性
     */
    private int divideLineColor = Color.RED;
    private int selectedItemColor = Color.BLUE;
    private int unselectedItemColor = Color.GREEN;
    private int selectedItemTextSize = 18;
    private int unselectedItemTextSize = 14;
    private int visibleItemNum = 5;
    private int selectedItemOffset = 2;
    private OnScrollSelectListener onScrollSelectListener;
    private int selectedItemPosition;

    private Paint mBgPaint;
    private int mItemHeight;
    private int mItemWidth;
    private int mFirstLineY;
    private int mSecondLineY;
    private boolean mFirstAmend;
    private int mInitialY;
    private Runnable mSmoothScrollTask;
    private int maxItemH;
    private int maxItemW;
    private List dataList;

    public MyScrollPicker(@NonNull Context context) {
        this(context,null);
    }

    public MyScrollPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public MyScrollPicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTask();
    }

    /**
     * 在ScrollPickerView滚动结束后调整item视图的位置，使得被选中的item视图刚好位于两条分割线中。
     */
    private void initTask() {
        mSmoothScrollTask = new Runnable() {
            @Override
            public void run() {
                int newY = getScrollYDistance();
                if (mInitialY != newY) {//这个比较是为了处理在mSmoothScrollTask刚要执行的时候用户又突然滑动的状况，这种状况下显然没有必要进行调整，所以直接结束当前任务，然后再触发一次mSmoothScrollTask任务即可。
                    mInitialY = getScrollYDistance();
                    postDelayed(mSmoothScrollTask, 30);
                } else if (mItemHeight > 0) {
                    final int offset = mInitialY % mItemHeight;//离选中区域中心的偏移量
                    if (offset == 0) {//item 刚好落在两条分割线的中间，无需调整
                        return;
                    }
                    if (offset >= mItemHeight / 2) {//滚动区域超过了item高度的1/2,item视图滚动到了两条分割线的中间现的下方 调整position的值
                        smoothScrollBy(0, mItemHeight - offset);
                    } else if (offset < mItemHeight / 2) {//中间偏上，向上调整（就是把那个上面只露出1/4的item给上移移出去
                        smoothScrollBy(0, -offset);
                    }
                }
            }
        };
    }

    /**
     * 为什么要复写这个方法？
     * 这个不是RecyclerView已经实现的方法吗？
     * 不实现有没有什么关系？
     * <p>
     * 答案是需要进行实现。原因阐述如下：
     * <p>
     * 首先，我们无法控制外部如何使用ScrollPickerView，
     * 比如可能高度填充满父视图，也可能是高度只有1dp，
     * 那么这个时候如果不复写onMeasure方法，
     * 滚动选择器要么填充屏幕进而错乱，要么变得看不到，
     * 此时我们必须要保证ScrollPickerView有个合适可用的可视化视图。
     * <p>
     * 而这个可视化视图就是根据item视图的大小来自适应的，
     * 因此，我们需要在onMeasure中完成子视图大小的测量，
     * 并以此来设置ScrollPickerView的高度和宽度，
     * 这样就达到了高宽度自适应，而不因外部设置的属性影响。
     * <p>
     * widthSpec设置成UNSPECIFIED，表示其宽度是不受限制的，
     * 而将heightSpec设置成AT_MOST(可以理解为对应于wrap_content)表示会根item视图高度完成滚动选择器的高度测量。
     * <p>
     * 设置好MeasureSpec后，我们调用了 super.onMeasure(widthSpec, heightSpec);因为原来父view传给我们的MeasureSpec并不是这样的。
     *
     * @param widthSpec
     * @param heightSpec
     */
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, heightSpec);
        measureSize();
        setMeasuredDimension(mItemWidth, mItemHeight * visibleItemNum);
    }


    /**
     * 获取item视图的高度和宽度，并完成两条分割线的Y坐标测绘
     */
    private void measureSize() {
        if (getChildCount() > 0) {
            if (mItemHeight == 0) {
                mItemHeight = getChildAt(0).getMeasuredHeight();
            }
            if (mItemWidth == 0) {
                mItemWidth = getChildAt(0).getMeasuredWidth();
            }
            if (mFirstLineY == 0 || mSecondLineY == 0) {
                mFirstLineY = mItemHeight * selectedItemOffset;
                mSecondLineY = mItemHeight * (selectedItemOffset + 1);
            }
        }
    }

    public void setSelectedPosition(int position) {
        selectedItemPosition = position;
    }


    /**
     * 复写onDraw方法的目的主要是完成两条分割线的绘制。我们在自定义view的时候会很方便的通过onDraw方法绘制各种图形，和此处复写onDraw的道理是一样的。
     *
     * @param c
     */
    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        doDraw(c);
        if (!mFirstAmend) {
            mFirstAmend = true;
            ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(selectedItemOffset, 0);
        }
    }

    private void doDraw(Canvas c) {
        if (mItemHeight > 0) {
            int screenX = getWidth();
            int startX = screenX / 2 - mItemWidth / 2 - ScreenUtil.dpToPx(5);
            int stopX = mItemWidth + startX + ScreenUtil.dpToPx(5);
            c.drawLine(startX, mFirstLineY, stopX, mFirstLineY, mBgPaint);
            c.drawLine(startX, mSecondLineY, stopX, mSecondLineY, mBgPaint);
        }
    }

    /**
     * 因为我们要监听滚动，所以我们要复写onScrolled方法。
     *
     * @param dx
     * @param dy
     */
    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        freshItemView();
    }

    /**
     * 判断整个item当中到底哪一个item被选中，判断结果传给方法
     */
    private void freshItemView() {
        for (int i = 0; i < getChildCount(); i++) {
            float itemViewY = getChildAt(i).getTop() + mItemHeight / 2;
            updateView(getChildAt(i), mFirstLineY < itemViewY && itemViewY < mSecondLineY);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        smoothScrollToPosition(selectedItemOffset + selectedItemPosition);
    }

    /**
     * 把itemView 有没有被选中这个结果传给 接口IPickerViewOperation の updateView 方法
     *
     * @param itemView
     * @param isSelected
     */
    private void updateView(View itemView, boolean isSelected) {
        TextView textView = itemView.findViewById(R.id.tv_content);
        textView.setTextSize(isSelected ? selectedItemTextSize : unselectedItemTextSize);
        textView.setTextColor(isSelected ? selectedItemColor : unselectedItemColor);
        int h = itemView.getHeight();
        if (h > maxItemH) {
            maxItemH = h;
        }
        int w = itemView.getWidth();
        if (w > maxItemW) {
            maxItemW = w;
        }
        itemView.setMinimumHeight(maxItemH);
        itemView.setMinimumWidth(maxItemW);
        if (isSelected && onScrollSelectListener != null) {
            onScrollSelectListener.onScroll(itemView);

        }
    }

    /**
     * 为什么复写该方法？
     * 这是因为RecyclerView的滚动我们是无法控制的，
     * 也就是说RecyclerView滚动结束后可以停在任何位置，
     * 换句话说，其中的item视图也可能会停在任意位置上。
     * 但是当前我们的需求是：当RecyclerView滚动结束的时候，
     * 必须要将item滚动到合适的位置，比如RecyclerView滚动结束的时候，
     * 可能被选中的item没有恰好位于两条分割线中间，那么这个就需要进行调整，
     * 使其滚动到分割线中间。这就可以通过监听onTouchEvent中的action up事件来解决。
     * <p>
     * 复写onTouchEvent方法，
     * 复写onTouchEvent方法就是为了解决上述的第二个问题，
     * 即解决滚动结束后被选中的item视图必须要位于两条分割线的正中间，
     * 因此我们要在用户手指离开屏幕的时候进行调整，
     * 这就需要监听ACTION_UP事件，
     *
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            processItemOffset();
        }
        return super.onTouchEvent(e);
    }

    /**
     * 滚动结束后，对recycler view进行调整，使得被选中的item处在两条分割线的正中间
     */
    private void processItemOffset() {
        mInitialY = getScrollYDistance();
        postDelayed(mSmoothScrollTask, 30);
    }

    private int getScrollYDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) this.getLayoutManager();
        if (layoutManager == null) {
            return 0;
        }
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        if (firstVisibleChildView == null) {
            return 0;
        }
        int itemHeight = firstVisibleChildView.getHeight();
        return (position) * itemHeight - firstVisibleChildView.getTop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initPaint();
        MyScrollPickerAdapter myScrollPickerAdapter = new MyScrollPickerAdapter(dataList, selectedItemOffset, visibleItemNum, selectedItemTextSize);
        setAdapter(myScrollPickerAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        setLayoutManager(linearLayoutManager);
//        if(selectedItemPosition != null){
        setSelectedPosition(selectedItemPosition);
//        }else{

//        }
    }

    /**
     * 进行画笔的初始化，这个画笔是指用于绘制两条分割线的画笔
     */
    private void initPaint() {
        if (mBgPaint == null) {
            mBgPaint = new Paint();
            mBgPaint.setColor(divideLineColor);
            mBgPaint.setStrokeWidth(1.0f);
        }
    }

    public void setOnScrollSelectListener(OnScrollSelectListener onScrollSelectListener) {
        this.onScrollSelectListener = onScrollSelectListener;
    }

    public void setDataList(List dataList) {
        this.dataList = dataList;
    }
}
