package com.sidcoparking.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sidcoparking.R;

import java.util.List;
import java.util.Map;

public class SettlementAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    ProgressDialog progressDialog;

    List<Map<String, String>> filterData;

    public SettlementAdapter(Activity context, List<Map<String, String>> listCollectionone) {
        activity = context;
        filterData = listCollectionone;

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

    }

    @Override
    public int getCount() {
        return this.filterData.size();
    }

    @Override
    public Object getItem(int position) {
        return this.filterData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {

        return 1;
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    public void pos(int position) {

        filterData.remove(filterData.get(position));

    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;

        if (vi == null) {

            vi = inflater.inflate(R.layout.settlement_adapter, null);
            holder = new ViewHolder();
            //Textview
            holder.Txttotal = vi.findViewById(R.id.total);
            holder.TxtFEName = vi.findViewById(R.id.name);
            holder.TxtShiftIn = vi.findViewById(R.id.intime);
            holder.TxtShiftOut = vi.findViewById(R.id.outtime);
            holder.TxtFOCCount = vi.findViewById(R.id.isfoc);
            holder.TxtFineAmount = vi.findViewById(R.id.fine_amt);
            holder.TxtCashCollection = vi.findViewById(R.id.cash_coll);
            holder.TxtCardCollection = vi.findViewById(R.id.card_coll);
            holder.TxtCollectionAmount = vi.findViewById(R.id.collection_amt);
            holder.TxtTicketCount = vi.findViewById(R.id.ticketcount);
            //Layout
            holder.Lyname = vi.findViewById(R.id.name_ly);
            holder.LyIn = vi.findViewById(R.id.intime_ly);
            holder.LyOut = vi.findViewById(R.id.outtime_ly);
            holder.LyFoc = vi.findViewById(R.id.isfoc_ly);
            holder.FineAmountLy = vi.findViewById(R.id.fine_amt_ly);
            holder.CashCollectionLy = vi.findViewById(R.id.cash_coll_ly);
            holder.CardCollectionLy = vi.findViewById(R.id.card_coll_ly);
            holder.CollectionAmountLy = vi.findViewById(R.id.coll_amt_ly);
            holder.Lytotal = vi.findViewById(R.id.total_ly);

            //new textview
            holder.Txt2WAmount = vi.findViewById(R.id.two_wheeler_amount);
            holder.Txt4WAmount = vi.findViewById(R.id.four_wheeler_amount);
            holder.Txt2WCount = vi.findViewById(R.id.two_wheeler_count);
            holder.Txt4WCount = vi.findViewById(R.id.four_wheeler_count);
            holder.Txt2WFineAmount = vi.findViewById(R.id.two_fine_amount);
            holder.Txt4WFineAmount = vi.findViewById(R.id.four_fine_amount);
            holder.Txt2WFocCount = vi.findViewById(R.id.foc_count_two);
            holder.Txt4WFocCount = vi.findViewById(R.id.foc_count_four);
            holder.Txt2WGraceCount = vi.findViewById(R.id.gracecount_two);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        if (filterData.get(position).get(
                "FEName") != null) {
            holder.Lyname.setVisibility(View.VISIBLE);
            holder.TxtFEName.setText(filterData.get(position).get(
                    "FEName"));
        } else {
            holder.Lyname.setVisibility(View.GONE);
        }
        //LyTicketCount
        if (filterData.get(position).get(
                "TicketCount") != null) {
            //holder.LyTicketCount.setVisibility(View.VISIBLE);
            holder.TxtTicketCount.setText(filterData.get(position).get(
                    "TicketCount"));
        } else {
            holder.Lyname.setVisibility(View.GONE);
        }

        if (filterData.get(position).get(
                "ShiftIn") != null) {
            holder.LyIn.setVisibility(View.VISIBLE);
            holder.TxtShiftIn.setText(filterData.get(position).get(
                    "ShiftIn"));
        } else {
            holder.LyIn.setVisibility(View.GONE);
        }

        if (filterData.get(position).get(
                "ShiftOut") != null) {
            holder.LyOut.setVisibility(View.VISIBLE);
            holder.TxtShiftOut.setText(filterData.get(position).get(
                    "ShiftOut"));
        } else {
            holder.LyOut.setVisibility(View.GONE);
        }

        if (filterData.get(position).get(
                "FOCCount") != null) {
            holder.LyFoc.setVisibility(View.VISIBLE);
            holder.TxtFOCCount.setText(filterData.get(position).get(
                    "FOCCount"));
        } else {
            holder.LyFoc.setVisibility(View.GONE);
        }

        if (filterData.get(position).get(
                "FineAmount") != null) {
            holder.FineAmountLy.setVisibility(View.VISIBLE);
            holder.TxtFineAmount.setText(filterData.get(position).get(
                    "FineAmount"));
        } else {
            holder.FineAmountLy.setVisibility(View.GONE);
        }
        if (filterData.get(position).get(
                "CashCollection") != null) {
            holder.CashCollectionLy.setVisibility(View.VISIBLE);
            holder.TxtCashCollection.setText(filterData.get(position).get(
                    "CashCollection"));
        } else {
            holder.CashCollectionLy.setVisibility(View.GONE);
        }

        if (filterData.get(position).get(
                "CardCollection") != null) {
            holder.CardCollectionLy.setVisibility(View.VISIBLE);
            holder.TxtCardCollection.setText(filterData.get(position).get(
                    "CardCollection"));
        } else {
            holder.CardCollectionLy.setVisibility(View.GONE);
        }

        if (filterData.get(position).get(
                "CollectionAmount") != null) {
            holder.CollectionAmountLy.setVisibility(View.VISIBLE);
            holder.TxtCollectionAmount.setText(filterData.get(position).get(
                    "CollectionAmount"));
        } else {
            holder.CollectionAmountLy.setVisibility(View.GONE);
        }

        if (filterData.get(position).get(
                "SummaryAmount") != null) {
            holder.Lytotal.setVisibility(View.VISIBLE);
            holder.Txttotal.setText(filterData.get(position).get(
                    "SummaryAmount"));
        } else {
            holder.Lytotal.setVisibility(View.GONE);
        }

        holder.Txt2WAmount.setText(filterData.get(position).get(
                "TwAmount"));
        holder.Txt4WAmount.setText(filterData.get(position).get(
                "FwAmount"));
        holder.Txt2WCount.setText(filterData.get(position).get(
                "TwCnt"));
        holder.Txt4WCount.setText(filterData.get(position).get(
                "FwCnt"));
        holder.Txt2WFineAmount.setText(filterData.get(position).get(
                "TwFineAmount"));
        holder.Txt4WFineAmount.setText(filterData.get(position).get(
                "FwFineAmount"));
        holder.Txt2WFocCount.setText(filterData.get(position).get(
                "TwFOCCnt"));
        holder.Txt4WFocCount.setText(filterData.get(position).get(
                "FwFOCCnt"));
        holder.Txt2WGraceCount.setText(filterData.get(position).get(
                "TwGraceCnt"));

        return vi;
    }

    public static class ViewHolder {
//LyTicketCount
        TextView TxtFEName,Txttotal, TxtShiftIn, TxtTicketCount, TxtShiftOut, TxtFOCCount, TxtFineAmount, TxtCollectionAmount, TxtCashCollection, TxtCardCollection;
        LinearLayout Lyname, LyIn, LyOut,  LyFoc, CollectionAmountLy, CashCollectionLy, CardCollectionLy;
        LinearLayout Lytotal, FineAmountLy;
        TextView Txt2WAmount,Txt4WAmount,Txt2WCount,Txt4WCount,Txt2WFineAmount,Txt4WFineAmount,Txt2WFocCount,Txt4WFocCount,Txt2WGraceCount;
    }

}
