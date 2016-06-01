package com.olayinka.smart.tone.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;

import com.olayinka.smart.tone.AppSqlHelper;

import java.util.LinkedList;

/**
 * Created by Olayinka on 8/21/2015.
 */
public class SelectionCursor extends MatrixCursor {
    private static final String SELECTION_ID = Media.Columns._ID + " = ?";
    private final int mColumnCount;

    public SelectionCursor(String[] columnNames, int initialCapacity) {
        super(columnNames, initialCapacity);
        mColumnCount = columnNames.length;
    }

    public SelectionCursor(String[] columnNames) {
        super(columnNames);
        mColumnCount = columnNames.length;
    }

    public SelectionCursor(Context context, OrderedMediaSet<Long> selection) {
        this(columnNames(context));
        addRows(context, selection);
    }

    private void addRows(Context context, OrderedMediaSet<Long> selection) {
        LinkedList<Long> ids = selection.getList();
        SQLiteDatabase database = AppSqlHelper.instance(context).getReadableDatabase();
        for (Long id : ids) {
            Cursor cursor = database.query(Media.TABLE, new String[]{"*"}, SELECTION_ID, new String[]{"" + id}, null, null, null);
            cursor.moveToNext();
            String[] row = new String[mColumnCount];
            for (int i = 0; i < mColumnCount; i++)
                row[i] = cursor.getString(i);
            addRow(row);
            cursor.close();
        }
    }

    private static String[] columnNames(Context context) {
        SQLiteDatabase database = AppSqlHelper.instance(context).getReadableDatabase();
        Cursor cursor = database.query(Media.TABLE, new String[]{"*"}, SELECTION_ID, new String[]{"" + 0}, null, null, null);
        String[] names = cursor.getColumnNames();
        cursor.close();
        return names;
    }

}
