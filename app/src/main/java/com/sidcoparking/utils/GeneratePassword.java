package com.sidcoparking.utils;

public class GeneratePassword {

    public static String GeneratePasswd() {

        String[] Passwd = new String[12];
        int iforth;
        int ithird;

        int[] FirstEdt = {8, 9, 0, 1, 2, 7, 9, 4, 2, 6, 0};
        int[] SecondEdt = {1, 7, 9, 2, 4, 6, 8, 0, 3, 5, 7};
        int[] ThirdEdt = {6, 8, 1, 3, 5, 7, 9, 2, 4, 6, 0};
        int[] FourthEdt = {5, 3, 1, 7, 4, 8, 6, 2, 0, 9, 5};

        int isecond;
        int ifirst;
        int k;

        int Rslt0, Rslt1, Rslt2, Rslt3, Rslt4, Rslt5, Rslt6, Rslt7, Rslt8, Rslt9, Rslt10;
        int Rslt11, Rslt12, Rslt13, Rslt14, Rslt15;
        //,Rslt16,Rslt17,Rslt18,Rslt19;

        String dates = Util.getdate();
        // 28012019
        String iDay = dates.substring(0, 2);
       //  Util.Logcat.e("iDay", iDay);
        String iMonth = dates.substring(2, 4);
      //   Util.Logcat.e("iMonth", iMonth);
        String iYr = dates.substring(4, 8);
      //   Util.Logcat.e("iYr", iYr);

        String cReportDt = iDay;
        String cReportMnth = iMonth;
        String cReportYr = iYr;

        String first = Character.toString(cReportDt.charAt(0));
     //    Util.Logcat.e("cReportDt1", first);

        ifirst = Integer.parseInt(first);

        String second = cReportDt.substring(1, 2);
      //   Util.Logcat.e("cReportDt2", second);

        isecond = Integer.parseInt(second);

        Rslt0 = FirstEdt[ifirst];
        Rslt1 = FourthEdt[ifirst];

        k = 11 - 1;

        Rslt13 = FirstEdt[k - ifirst];
        Rslt2 = FirstEdt[isecond];
        Rslt3 = FourthEdt[isecond];
        k = 11 - 1;

        Rslt12 = FirstEdt[k - isecond];

        isecond = 0;
        ifirst = 0;

        first = Character.toString(cReportMnth.charAt(0));
     //    Util.Logcat.e("cReportMnth", first);

        ifirst = Integer.parseInt(first);

        second = cReportMnth.substring(1, 2);
     //    Util.Logcat.e("cReportMnth2", second);

        isecond = Integer.parseInt(second);

        Rslt4 = FourthEdt[ifirst];
        Rslt5 = SecondEdt[isecond];
        k = 11 - 1;

        Rslt14 = FirstEdt[k - ifirst];
        Rslt6 = SecondEdt[isecond];
        Rslt7 = FirstEdt[ifirst];
        k = 11 - 1;
        Rslt15 = FirstEdt[k - ifirst];

        isecond = 0;
        ifirst = 0;

        first = Character.toString(cReportYr.charAt(0));
      //   Util.Logcat.e("cReportYr", first);

        ifirst = Integer.parseInt(first);
        second = cReportYr.substring(1, 2);
     //    Util.Logcat.e("cReportYr", second);

        isecond = Integer.parseInt(second);

        String forth = Character.toString(cReportYr.charAt(2));
      //   Util.Logcat.e("cReportYr", forth);

        iforth = Integer.parseInt(forth);

        String third = Character.toString(cReportYr.charAt(3));
      //   Util.Logcat.e("cReportYr", third);

        ithird = Integer.parseInt(third);

        Rslt8 = ThirdEdt[ifirst];
        Rslt9 = ThirdEdt[ithird];
        Rslt10 = ThirdEdt[iforth];
        Rslt11 = ThirdEdt[isecond];

        Passwd[0] = String.valueOf(Rslt15);
        Passwd[1] = String.valueOf(Rslt0);
        Passwd[2] = String.valueOf(Rslt1);
        Passwd[3] = String.valueOf(Rslt8);

        Passwd[4] = String.valueOf(Rslt2);
        Passwd[5] = String.valueOf(Rslt3);
        Passwd[6] = String.valueOf(Rslt9);
        Passwd[7] = String.valueOf(Rslt4);

        Passwd[8] = String.valueOf(Rslt5);
        Passwd[9] = String.valueOf(Rslt6);
        Passwd[10] = String.valueOf(Rslt7);
        Passwd[11] = String.valueOf(Rslt10);

        String hai = Passwd[0] + Passwd[1] + Passwd[2] + Passwd[3] + Passwd[4] + Passwd[5] + Passwd[6] + Passwd[7] + Passwd[8] + Passwd[9] + Passwd[10] + Passwd[11];
         Util.Logcat.e("Passwd"+ hai);
        return hai;

    }

}
