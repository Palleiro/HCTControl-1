package com.hctrom.romcontrol.licenseadapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hctrom.romcontrol.R;
import com.hctrom.romcontrol.licenseadapter.internal.ContentViewHolder;
import com.hctrom.romcontrol.licenseadapter.internal.ContentWrapper;
import com.hctrom.romcontrol.licenseadapter.internal.HeaderViewHolder;
import com.hctrom.romcontrol.licenseadapter.internal.HeaderWrapper;
import com.hctrom.romcontrol.licenseadapter.internal.LicenseViewHolder;
import com.hctrom.romcontrol.licenseadapter.internal.ViewType;
import com.hctrom.romcontrol.licenseadapter.internal.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yshrsmz on 2016/04/26.
 */
public class LicenseAdapter extends RecyclerView.Adapter<LicenseViewHolder> {

  private final List<LicenseEntry> originalDataSet = new ArrayList<>();
  private final List<Wrapper> wrappedDataSet = new ArrayList<>();

  public LicenseAdapter(List<LicenseEntry> entries) {
    originalDataSet.addAll(entries);

    for (LicenseEntry entry : originalDataSet) {
      wrappedDataSet.add(new HeaderWrapper(entry));
    }
  }

  @Override
  public int getItemViewType(int position) {
    Wrapper wrapper = getItem(position);

    if (wrapper == null) {
      throw new IllegalStateException("No wrapper for this position: " + position);
    }
    return wrapper.type().ordinal();
  }

  @Override
  public LicenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    ViewType type = ViewType.values()[viewType];
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View view;

    switch (type) {
      case HEADER:
        view = inflater.inflate(R.layout.license_header, parent, false);
        return new HeaderViewHolder(view);

      case CONTENT:
        view = inflater.inflate(R.layout.license_content, parent, false);
        return new ContentViewHolder(view);

      default:
        throw new IllegalArgumentException(
            "No ViewHolder exists for ViewType: " + String.valueOf(viewType));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onBindViewHolder(final LicenseViewHolder holder, final int position) {
    final Wrapper wrapper = getItem(position);

    if (wrapper == null) {
      throw new IllegalStateException("No wrapper for this position: " + position);
    }

    ViewType viewType = wrapper.type();
    // bind variable, not event
    holder.bind(viewType.convert(wrapper));

    switch (viewType) {
      case HEADER:
        holder.itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int position = holder.getAdapterPosition();

            if (!wrapper.entry().hasContent()) {
              return;
            }

            boolean expanded = wrapper.isExpanded();

            if (expanded) {
              int removed = collapse(position);
              notifyItemChanged(position);
              notifyItemRemoved(removed);
            } else {
              int added = expand(position);
              notifyItemChanged(position);
              notifyItemInserted(added);
            }
          }
        });
        break;
      case CONTENT:
        holder.itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int position = holder.getAdapterPosition();

            int removed = collapse(position - 1);
            notifyItemChanged(position - 1);
            notifyItemRemoved(removed);
          }
        });
        break;
    }
  }

  @SuppressWarnings("ConstantConditions")
  private int expand(int headerPosition) {
    int added = headerPosition + 1;
    Wrapper headerItem = getItem(headerPosition);
    headerItem.setExpanded(true);

    ContentWrapper contentWrapper = new ContentWrapper(headerItem.entry());

    wrappedDataSet.add(added, contentWrapper);
    return added;
  }

  private int collapse(int headerPosition) {
    Wrapper headerItem = getItem(headerPosition);
    Wrapper contentItem = getItem(headerPosition + 1);

    headerItem.setExpanded(false);
    contentItem.setExpanded(false);

    int removed = headerPosition + 1;
    wrappedDataSet.remove(removed);

    return removed;
  }

  @Override
  public int getItemCount() {
    return wrappedDataSet.size();
  }

  private Wrapper getItem(int position) {
    boolean isIndexInRange = position >= 0 && position < wrappedDataSet.size();
    return isIndexInRange ? wrappedDataSet.get(position) : null;
  }
}
