package com.example.shadicomapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.shadicomapp.R;
import com.example.shadicomapp.database.DatabaseHelper;
import com.example.shadicomapp.model.Result;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.shadicomapp.database.DatabaseHelper.version_val;

public class CardInfoAdapter extends RecyclerView.Adapter<CardInfoAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Result> results;
    private LayoutInflater layoutInflater;
    private DatabaseHelper mDbHelper;
    private boolean flag = false;

    public CardInfoAdapter(Context context, ArrayList<Result> results) {
        this.context = context;
        this.results = results;

        mDbHelper = new DatabaseHelper(context, "Database.sqlite", null, version_val);
        mDbHelper = DatabaseHelper.getDBAdapterInstance(context);

        try {
            mDbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CardInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint({"SetTextI18n", "CheckResult"})
    @Override
    public void onBindViewHolder(@NonNull final CardInfoAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.txtName.setText("Name      : \t" + results.get(position).getName().getTitle() + " " + results.get(position).getName().getFirst() + " " + results.get(position).getName().getLast());
        viewHolder.txtGender.setText("Gender    : \t" + results.get(position).getGender());
        viewHolder.txtMob.setText("Phone     : \t" + results.get(position).getPhone());
        viewHolder.txtCell.setText("Cell          : \t" + results.get(position).getCell());
        viewHolder.txtAge.setText("Age          : \t" + results.get(position).getDob().getAge());
        viewHolder.txtEmail.setText("Email       : \t" + results.get(position).getEmail());
        viewHolder.txtAddress.setText("Address : \t" + results.get(position).getLocation().getStreet().getNumber() + ", " +
                results.get(position).getLocation().getStreet().getName() + ", " + results.get(position).getLocation().getCity() + ", " +
                results.get(position).getLocation().getState() + ", " + results.get(position).getLocation().getCountry() + ", " +
                results.get(position).getLocation().getPostcode() + ".");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
        Date d = null;
        try {
            d = input.parse(results.get(position).getDob().getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert d != null;
        String formatted = output.format(d);
        Log.i("DATE", "" + formatted);

        viewHolder.txtDob.setText("DOB         : \t" + formatted);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        requestOptions.error(R.mipmap.ic_launcher);

        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .asBitmap()
                .load(results.get(position).getPicture().getLarge())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.mipmap.ic_launcher)
                .fitCenter()
                .into(viewHolder.itemImage);

        String msg;
        if (flag) {
            msg = context.getResources().getString(R.string.acceptMsg);
        } else {
            msg = context.getResources().getString(R.string.declineMsg);
        }

        String emailId = mDbHelper.retrieveStatus(results.get(position).getEmail(), msg);
        if (results.get(position).getEmail().equalsIgnoreCase(emailId)) {
            if (viewHolder.txtMsg.equals("")) {
                viewHolder.btn_accept.setVisibility(View.VISIBLE);
                viewHolder.btn_decline.setVisibility(View.VISIBLE);
            } else {
                viewHolder.txtMsg.setVisibility(View.VISIBLE);
                viewHolder.txtMsg.setText(msg);
                viewHolder.btn_accept.setVisibility(View.GONE);
                viewHolder.btn_decline.setVisibility(View.GONE);
            }
        }

        viewHolder.btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = true;
                mDbHelper.addStatus(results.get(position).getEmail(), context.getString(R.string.acceptMsg));
                viewHolder.txtMsg.setVisibility(View.VISIBLE);
                viewHolder.txtMsg.setText(R.string.acceptMsg);
                viewHolder.txtMsg.setTextColor(context.getResources().getColor(R.color.bgAccept));
                viewHolder.btn_accept.setVisibility(View.GONE);
                viewHolder.btn_decline.setVisibility(View.GONE);
            }
        });

        viewHolder.btn_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = false;
                mDbHelper.addStatus(results.get(position).getEmail(), context.getString(R.string.declineMsg));
                viewHolder.txtMsg.setVisibility(View.VISIBLE);
                viewHolder.txtMsg.setText(R.string.declineMsg);
                viewHolder.txtMsg.setTextColor(context.getResources().getColor(R.color.bgDecline));
                viewHolder.btn_accept.setVisibility(View.GONE);
                viewHolder.btn_decline.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView itemImage;
        public TextView txtName;
        public TextView txtDob;
        public TextView txtGender;
        public TextView txtMob;
        public TextView txtAge;
        public Button btn_accept;
        public Button btn_decline;
        public TextView txtMsg;
        public TextView txtCell;
        public TextView txtEmail;
        public TextView txtAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemImage = itemView.findViewById(R.id.item_image);
            txtName = itemView.findViewById(R.id.txt_name);
            txtDob = itemView.findViewById(R.id.txt_dob);
            txtGender = itemView.findViewById(R.id.txt_gender);
            txtMob = itemView.findViewById(R.id.txt_mobile);
            txtAge = itemView.findViewById(R.id.txt_age);
            btn_accept = itemView.findViewById(R.id.btn_accept);
            btn_decline = itemView.findViewById(R.id.btn_decline);
            txtMsg = itemView.findViewById(R.id.txt_msg);
            txtCell = itemView.findViewById(R.id.txt_cell);
            txtEmail = itemView.findViewById(R.id.txt_email);
            txtAddress = itemView.findViewById(R.id.txt_address);

        }
    }
}
