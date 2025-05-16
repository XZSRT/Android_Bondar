package com.example.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class PasswordAdapter extends ArrayAdapter<PasswordModel> {
    public PasswordAdapter(Context context, List<PasswordModel> passwords) {
        super(context, 0, passwords);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PasswordModel password = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_password, parent, false);
        }

        TextView tvService = convertView.findViewById(R.id.tvService);
        TextView tvLogin = convertView.findViewById(R.id.tvLogin);
        TextView tvPassword = convertView.findViewById(R.id.tvPassword);

        tvService.setText(password.getService());
        tvLogin.setText(password.getLogin());
        tvPassword.setText("••••••••");

        return convertView;
    }
}
