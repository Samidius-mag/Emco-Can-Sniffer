package com.android.emcocansniffer;

import android.widget.TextView;
public class DataSearchHelper {
    public static void searchDataById(int id, int[] data, TextView textView) {
// Фильтр по ID
        if (id == 0x18FF83DD) {
// Группировка данных по 2 знака
            StringBuilder groupedData = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                String hex = String.format("%04X", data[i]); // Преобразуем значение в шестнадцатеричную строку
                for (int j = 0; j < hex.length(); j += 2) {
                    groupedData.append(hex.substring(j, j + 2)).append(" ");
                }
            }
// Получим второе сгруппированное значение и отобразим его на дисплее
            String[] splitData = groupedData.toString().split(" ");
            if (splitData.length > 1) {
                int secondGroupedValue = Integer.parseInt(splitData[3], 16); // SplitData[i], где [i] кроме 1 = актет 1++. (Для 2 - 3, для 3 - 5, 4 - 7 и т.д.)
// String decimalValue = Integer.toString(secondGroupedValue); // Преобразуем значение в десятичную систему счисления
                textView.setText("Количество спутников: " + secondGroupedValue);
            }
        }
    }
}
