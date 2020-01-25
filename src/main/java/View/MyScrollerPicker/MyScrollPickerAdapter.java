package View.MyScrollerPicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sadam.sadamlibarary.R;

import java.util.List;

public class MyScrollPickerAdapter<T> extends RecyclerView.Adapter<MyScrollPickerViewHolder> {
    private List<T> mDataList;
    private int visibleItemNum;
    private int selectedItemOffset;
    private int textSize;

    public MyScrollPickerAdapter(List list, int selectedItemOffset, int visibleItemNum, int textSize) {
        setData(list, selectedItemOffset, visibleItemNum, textSize);
    }


    @NonNull
    @Override
    public MyScrollPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scroll_picker_default_item_layout, parent, false);
        return new MyScrollPickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyScrollPickerViewHolder holder, int position) {
        T data = mDataList.get(position);
        holder.textView.setText(data == null ? "" : data.toString());
        holder.itemView.setTag(mDataList.get(position));
        holder.textView.setTextSize(18);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void setData(List list, int selectedItemOffset, int visibleItemNum, int textSize) {
        this.selectedItemOffset = selectedItemOffset;
        this.visibleItemNum = visibleItemNum;
        this.textSize = textSize;
        mDataList = list;
        adaptiveData();
    }

    private void adaptiveData() {
        for (int i = 0; i < selectedItemOffset; i++) {
            mDataList.add(0, null);//在滚动器前面增加数据，item数据值为空
        }
        for (int i = 0; i < visibleItemNum - selectedItemOffset - 1; i++) {
            mDataList.add(null);//在滚动器后面增加数据，item数值为空
        }
    }
}
