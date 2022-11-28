package com.betosof.databases2;

import androidx.annotation.NonNull;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity {
    private static final int ADD_ID = Menu.FIRST + 1;
    private static final int DELETE_ID =Menu.FIRST + 3;
    private DatabaseHelper db = null;
    private Cursor constantsCursor = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);
        constantsCursor = db.getReadableDatabase().rawQuery("SELECT _ID,title,value FROM constants ORDER BY title",null);

        ListAdapter adapter =  new SimpleCursorAdapter(this, R.layout.row,constantsCursor, new String[] { DatabaseHelper.TITLE, DatabaseHelper.VALUE},new int[] {R.id.txtTitle,R.id.txtValue});
        setListAdapter(adapter);
        registerForContextMenu(getListView());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        constantsCursor.close();
        db.close();
    }

    public static class DialogWrapper {
        EditText campoTitulo;
        EditText campoValor;
        View base;

        public DialogWrapper(View base) {
            this.base = base;
            campoValor = base.findViewById(R.id.edtValor);
        }

        private EditText getCampoTitulo() {
            if (campoTitulo == null)
                campoTitulo = base.findViewById(R.id.edtTitulo);
            return campoTitulo;
        }

        public EditText getCampoValor() {
            if (campoValor == null)
                campoValor = base.findViewById(R.id.edtValor);
            return campoValor;
        }

        public String getTitulo() {
            return (getCampoTitulo().getText().toString());
        }

        public float getValor() {
            return Float.parseFloat(getCampoValor().getText().toString());
        }
    }

    private void add() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View addView = inflater.inflate(R.layout.add_edit,null);
        final DialogWrapper dialogWrapper = new DialogWrapper(addView);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add)
                .setView(addView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        processAdd(dialogWrapper);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //  No hacemos nada
                    }
                }).show();
    }

    private void delete(final long rowID) {
        if (rowID > 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            processDelete(rowID);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //  NMo hacemos nada
                        }
                    })
                    .show();

        }
    }

    public void processAdd(DialogWrapper dialogWrapper) {
        ContentValues contentValues = new ContentValues(2);

        contentValues.put(DatabaseHelper.TITLE, dialogWrapper.getTitulo());
        contentValues.put(DatabaseHelper.VALUE, dialogWrapper.getValor());

        db.getReadableDatabase().insert("constants", DatabaseHelper.TITLE,contentValues);
        constantsCursor.requery();
    }

    public void processDelete(long rowID) {
        String[] args = { String.valueOf(rowID) };
        db.getReadableDatabase().delete("constants", "_ID=?", args);
        constantsCursor.requery();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE,ADD_ID,Menu.NONE,"Agregar...").setAlphabeticShortcut('c');
        menu.add(Menu.NONE, DELETE_ID,Menu.NONE, "Borrar...").setAlphabeticShortcut('b');
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case ADD_ID: add(); break;
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                delete(info.id);
        }
        return super.onContextItemSelected(item);
    }
}