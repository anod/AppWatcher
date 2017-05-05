package com.anod.appwatcher.tags;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.android.colorpicker.ColorPickerSwatch;
import com.android.colorpicker.ColorStateDrawable;
import com.anod.appwatcher.R;
import com.anod.appwatcher.model.Tag;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.anodsplace.colorpicker.ColorPickerDialog;

/**
 * @author algavris
 * @date 14/04/2017.
 */

public class EditTagDialog extends DialogFragment implements ColorPickerSwatch.OnColorSelectedListener {

    @BindView(R.id.tag_name)
    TextInputEditText mEditText;
    @BindView(R.id.color_preview)
    ImageView mColor;
    @BindView(android.R.id.button3)
    Button mDeleteButton;

    private Tag mTag;


    public static EditTagDialog newInstance(@Nullable Tag tag) {
        EditTagDialog frag = new EditTagDialog();
        Bundle args = new Bundle();
        if (tag != null) {
            args.putParcelable("tag", tag);
        }
        frag.setArguments(args);

        frag.setStyle(STYLE_NO_TITLE, R.style.AppTheme_Dialog);
        return frag;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_tag, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTag = getArguments().getParcelable("tag");
        if (mTag == null) {
            mTag = new Tag("");
        }
        mEditText.setText(mTag.name);
        Drawable[] colorDrawable = new Drawable[] {
                ResourcesCompat.getDrawable(getResources(), R.drawable.color_picker_swatch, null)
        };
        mColor.setImageDrawable(new ColorStateDrawable(colorDrawable, mTag.color));
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        if (mTag.id == -1) {
            mDeleteButton.setVisibility(View.GONE);
        } else {
            mDeleteButton.setVisibility(View.VISIBLE);
        }
    }


    @OnClick(R.id.color_preview)
    void onColorClick()
    {
        ColorPickerDialog dialog = ColorPickerDialog.newInstance(mTag.color, false, getActivity());
        dialog.setOnColorSelectedListener(this);
        dialog.show(getFragmentManager(), "color-picker");
    }

    @OnClick(android.R.id.button1)
    void onSaveClick()
    {
        mTag = new Tag(mTag.id, mEditText.getText().toString().trim(), mTag.color);
        ((TagsListActivity)getActivity()).saveTag(mTag);
        dismiss();
    }

    @OnClick(android.R.id.button2)
    void onCancelClick()
    {
        dismiss();
    }

    @OnClick(android.R.id.button3)
    void onDeleteClick()
    {
        ((TagsListActivity)getActivity()).deleteTag(mTag);
        dismiss();
    }

    @Override
    public void onColorSelected(int color) {
        mTag = new Tag(mTag.id, mTag.name, color);

        Drawable[] colorDrawable = new Drawable[] {
                ResourcesCompat.getDrawable(getResources(), R.drawable.color_picker_swatch, null)
        };
        mColor.setImageDrawable(new ColorStateDrawable(colorDrawable, mTag.color));
    }
}
