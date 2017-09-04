package com.actions.bluetoothbox.widget;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actions.bluetoothbox.R;
import com.actions.ibluz.ota.updater.UpdatePartConfig;

import java.util.List;

public final class NumberPickerDialog extends AlertDialog {

    private NumberPickerListener listener;
    private String[] mDisplayedIds;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public NumberPickerDialog(final Context context, final UpdatePartConfig updatePartConfig) {
        super(context);
        if (Build.VERSION.SDK_INT >= 11) {
            RelativeLayout relativeLayout = new RelativeLayout(context);
            final NumberPicker aNumberPicker = new NumberPicker(context);
            List<Integer> partIds = updatePartConfig.getValidPartIds();
            aNumberPicker.setMaxValue(partIds.size()-1);
            aNumberPicker.setMinValue(0);
            mDisplayedIds = new String[partIds.size()];
            for (int i = 0; i < partIds.size(); i++) {
                mDisplayedIds[i] = String.valueOf(partIds.get(i)) ;
            }
            aNumberPicker.setDisplayedValues(mDisplayedIds);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
            RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            relativeLayout.setLayoutParams(params);
            relativeLayout.addView(aNumberPicker, numPickerParams);


            setTitle("Select the number");
            setView(relativeLayout);
            setCancelable(false);
            setButton(Dialog.BUTTON_POSITIVE, context.getText(R.string.action_submit), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null) {
                        listener.onPick(Integer.parseInt(mDisplayedIds[aNumberPicker.getValue()]));
                    }
                }
            });
            setButton(Dialog.BUTTON_NEGATIVE, context.getText(R.string.action_cancel), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null) {
                        listener.onCancel();
                    }
                }
            });
        } else {
            RelativeLayout relativeLayout = new RelativeLayout(context);
            final EditText editText = new EditText(context);

            editText.setHint(R.string.ota_part_order_limit);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
            RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            editTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            relativeLayout.setLayoutParams(params);
            relativeLayout.addView(editText, editTextParams);

            setTitle("Enter the number");
            setView(relativeLayout);

            setCancelable(false);

            //Onclick requirement
            setButton(Dialog.BUTTON_POSITIVE, context.getText(R.string.action_submit), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {

                    Button b = getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO Do something
                            int partId = Integer.parseInt(editText.getText().toString());
                            if (updatePartConfig.isValidFilePartId(partId)) {
                                Toast.makeText(context, R.string.ota_part_order_limit, Toast.LENGTH_SHORT).show();
                            } else {
                                if (listener != null) {
                                    listener.onPick(Integer.parseInt(editText.getText().toString()));
                                }
                                dismiss();
                            }
                        }
                    });

                }
            });

        setButton(Dialog.BUTTON_NEGATIVE, context.getText(R.string.action_cancel), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onCancel();
                }
            }
        });
        }
    }


    public void setNumberPickerListener(NumberPickerListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    public interface NumberPickerListener {
        void onPick(int number);

        void onCancel();
    }


} 