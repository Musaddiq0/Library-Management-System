package objParsing;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class GenericTableModel extends AbstractTableModel {
    List <String> columns;
    List<List<Object>> data;

    /**
     * this constructor gets the columns and data(row) for each data to be entered on the table
     * @param columns contains a list of string values parsed according to the method called
     * @param data contains a list of objects that contains the row information parsed according to the result gotten*/
    public GenericTableModel(List<String> columns, List<List<Object>> data){
        this.columns=columns;
        this.data=data;
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column);
    }
    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).get(columnIndex);
    }
}

