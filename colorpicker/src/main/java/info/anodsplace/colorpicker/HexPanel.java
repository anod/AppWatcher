package info.anodsplace.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.colorpicker.ColorStateDrawable;

/**
 * @author alex
 * @date 2014-12-17
 */
public class HexPanel extends LinearLayout {

    private final ImageView mPreview;

    private boolean mHexVisible;

    private EditText mHexEdit;

    private boolean mAlphaSupport;


    public HexPanel(Context context) {
        this(context, null);
    }

    public HexPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HexPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOrientation(HORIZONTAL);

        LayoutInflater.from(context).inflate(R.layout.color_picker_hex_panel, this);

        mHexEdit = (EditText) findViewById(R.id.hex_edit);
        mPreview = (ImageView) findViewById(R.id.hex_preview);
        if (isInEditMode()) {
            setPreviewColor(Color.RED);
        }
    }

    public void init(int color, boolean alphaSupport) {
        mAlphaSupport = alphaSupport;
        InputFilter filter0 = new InputFilter.LengthFilter((alphaSupport) ? 8 : 6);
        InputFilter filter1 = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                    int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char ch = source.charAt(i);
                    if (Character.isDigit(ch) || (ch >= 'A' && ch <= 'F') || (ch >= 'a'
                            && ch <= 'f')) {
                        return null;
                    } else {
                        return "";
                    }
                }
                return null;
            }
        };

        setColor(color);

        mHexEdit.setFilters(new InputFilter[]{filter0, filter1});
        mHexEdit.setVisibility(View.VISIBLE);
        mHexEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                if (!TextUtils.isEmpty(value)) {
                    int newColor = new ColorHex(value, mAlphaSupport, -1).intValue;
                    if (newColor != -1) {
                        setPreviewColor(newColor);
                    }
                }
            }
        });

    }

    public void setColor(int color) {
        mHexEdit.setText(new ColorHex(color, mAlphaSupport).stringValue);
        setPreviewColor(color);
    }

    protected void setPreviewColor(int color) {
        Drawable[] colorDrawable = new Drawable[] {
            ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.color_picker_swatch, null)
        };
        mPreview.setImageDrawable(new ColorStateDrawable(colorDrawable, color));
    }

    public void hide() {
        setVisibility(View.GONE);
        mHexVisible = false;
    }

    public void show() {
        setVisibility(View.VISIBLE);
        mHexVisible = true;
    }

    public boolean isVisible() {
        return mHexVisible;
    }

    public int getColor(int defaultColor) {
        return new ColorHex(mHexEdit.getText().toString(), mAlphaSupport, defaultColor).intValue;
    }


}
