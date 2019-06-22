package com.example.testcontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ConnectionDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText addrField = new EditText(getActivity());
        addrField.setHint("接続先ホスト名を入力");
        layout.addView(addrField);
        final EditText portField = new EditText(getActivity());
        portField.setHint("ポート番号を入力");
        portField.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(portField);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Raspberry Pi と接続");
        addrField.setText(SocketClientTask.DEFAULT_ADDRESS);
        portField.setText(Integer.valueOf(SocketClientTask.DEFAULT_PORT).toString());
        builder.setView(layout);
        builder.setPositiveButton("接続", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                String hostname = addrField.getText().toString();
                int port = Integer.parseInt(portField.getText().toString());
                SocketClientTask.address = hostname;
                SocketClientTask.port = port;
                SocketClientTask task = new SocketClientTask(getActivity());
                task.execute(SocketClientTask.CMD_OPEN);
            }
        });
        builder.setNeutralButton("切断", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                SocketClientTask task = new SocketClientTask(getActivity());
                task.execute(SocketClientTask.CMD_CLOSE);
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) { }
        });
        return builder.create();
    }
}