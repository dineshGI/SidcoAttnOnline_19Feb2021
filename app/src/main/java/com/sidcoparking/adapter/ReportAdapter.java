package com.sidcoparking.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sidcoparking.R;

import java.util.List;
import java.util.Map;

public class ReportAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    ProgressDialog progressDialog;

    List<Map<String, String>> filterData;

    public ReportAdapter(Activity context, List<Map<String, String>> listCollectionone) {
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

        return getCount();
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

            vi = inflater.inflate(R.layout.report_adapter, null);
            holder = new ViewHolder();
            holder.TxtShiftin = vi.findViewById(R.id.shiftin);
            holder.TxtShiftout = vi.findViewById(R.id.shiftout);
            holder.TxtTicektCount = vi.findViewById(R.id.ticker_count);
            holder.TxtTotalAmount = vi.findViewById(R.id.total_amt);
            holder.TxtTwoWheeler = vi.findViewById(R.id.two_wheeler);
            holder.TxtTwoWheelerCount = vi.findViewById(R.id.two_wheeler_count);
            holder.TxtFourWheeler = vi.findViewById(R.id.four_wheeler);
            holder.TxtFourWheelerCount = vi.findViewById(R.id.four_wheeler_count);
            holder.TxtFineAmount = vi.findViewById(R.id.fine_amt);
            holder.TxtFineAmountTwo = vi.findViewById(R.id.fine_amt_two);
            holder.TxtFineAmountFour = vi.findViewById(R.id.fine_amt_four);
            holder.TxtCashAmount = vi.findViewById(R.id.cash_amt);
            holder.TxtCardAmount = vi.findViewById(R.id.card_amt);

            holder.TxtFOCCount = vi.findViewById(R.id.foc_count);
            holder.TxtFOCCountTwo = vi.findViewById(R.id.foc_count_two);
            holder.TxtFOCCountFour = vi.findViewById(R.id.foc_count_four);
            holder.TxtTwoGrace = vi.findViewById(R.id.two_gracecount);

            holder.TxtStatus = vi.findViewById(R.id.status);
            holder.TxtHandoverTo = vi.findViewById(R.id.handover_to);
            holder.TxtHandoverDate = vi.findViewById(R.id.handover_date);

            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }
        holder.TxtShiftin.setText(filterData.get(position).get(
                "ShiftIn"));
        holder.TxtShiftout.setText(filterData.get(position).get(
                "ShiftOut"));
        holder.TxtTicektCount.setText(filterData.get(position).get(
                "TicketCount"));
        holder.TxtTotalAmount.setText(filterData.get(position).get(
                "TotalAmount"));

        holder.TxtTwoWheeler.setText(filterData.get(position).get(
                "TwoWheelerAmount"));
        holder.TxtTwoWheelerCount.setText(filterData.get(position).get(
                "TwoWheelerCount"));

        holder.TxtFourWheeler.setText(filterData.get(position).get(
                "FourWheelerAmount"));
        holder.TxtFourWheelerCount.setText(filterData.get(position).get(
                "FourWheelerCount"));

        holder.TxtFineAmount.setText(filterData.get(position).get(
                "FineAmount"));
        holder.TxtFineAmountTwo.setText(filterData.get(position).get(
                "TwFineAmount"));
        holder.TxtFineAmountFour.setText(filterData.get(position).get(
                "FwFineAmount"));

        holder.TxtCashAmount.setText(filterData.get(position).get(
                "CashCollection"));
        holder.TxtCardAmount.setText(filterData.get(position).get(
                "CardCollection"));
        holder.TxtFOCCount.setText(filterData.get(position).get(
                "FOCCount"));

        holder.TxtFOCCountTwo.setText(filterData.get(position).get(
                "TwFOCCnt"));
        holder.TxtFOCCountFour.setText(filterData.get(position).get(
                "FwFOCCnt"));
        holder.TxtTwoGrace.setText(filterData.get(position).get(
                "TwGraceCnt"));

        holder.TxtStatus.setText(filterData.get(position).get(
                "HandOverStatus"));

        holder.TxtHandoverTo.setText(filterData.get(position).get(
                "HandoverTo"));
        holder.TxtHandoverDate.setText(filterData.get(position).get(
                "HandoverDate"));

        return vi;
    }

    public static class ViewHolder {

        TextView TxtShiftin, TxtShiftout, TxtTotalAmount, TxtTicektCount, TxtFineAmount, TxtFOCCount, TxtStatus, TxtCashAmount, TxtCardAmount;
        TextView  TxtTwoWheeler, TxtFourWheeler,TxtTwoWheelerCount, TxtFourWheelerCount,TxtFineAmountTwo,TxtFineAmountFour;
        TextView TxtFOCCountTwo,TxtFOCCountFour,TxtTwoGrace,TxtHandoverTo,TxtHandoverDate;
    }

}
