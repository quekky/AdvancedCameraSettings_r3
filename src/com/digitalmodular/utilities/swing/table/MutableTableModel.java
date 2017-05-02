package com.digitalmodular.utilities.swing.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.table.AbstractTableModel;

/**
 * @author Mark Jeronimus
 */
// date 2005-08-07
public class MutableTableModel extends AbstractTableModel implements List<Object[]> {
	private ArrayList<Object[]>	tableData	= new ArrayList<Object[]>();
	private String[]			tableColumnNames;
	private boolean[]			editable;

	public MutableTableModel(String[] tableColumnNames) {
		super();

		tableData = new ArrayList<Object[]>();
		this.tableColumnNames = tableColumnNames;
		editable = new boolean[tableColumnNames.length];
		Arrays.fill(editable, false);
		super.fireTableRowsInserted(0, tableData.size() - 1);
	}

	public MutableTableModel(String[] tableColumnNames, boolean[] editable) {
		super();

		tableData = new ArrayList<Object[]>();
		this.tableColumnNames = tableColumnNames;
		this.editable = editable;
		super.fireTableRowsInserted(0, tableData.size() - 1);
	}

	public MutableTableModel(ArrayList<Object[]> tableData, String[] tableColumnNames, boolean[] editable) {
		super();

		this.tableData = tableData;
		this.tableColumnNames = tableColumnNames;
		this.editable = editable;
		super.fireTableRowsInserted(0, tableData.size() - 1);
	}

	@Override
	public boolean add(Object[] elements) {
		int i = size();
		tableData.add(elements);
		super.fireTableRowsInserted(i, i);
		return true;
	}

	@Override
	public void add(int index, Object[] element) {
		tableData.add(index, element);
		super.fireTableRowsInserted(index, index);
	}

	@Override
	public boolean addAll(Collection<? extends Object[]> c) {
		int len = c.size();
		if (len != 0) {
			int oldSize = tableData.size();
			tableData.addAll(c);
			super.fireTableRowsInserted(oldSize, oldSize + len - 1);
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Object[]> c) {
		int len = c.size();
		if (len != 0) {
			tableData.addAll(index, c);
			super.fireTableRowsInserted(index, index + len - 1);
			return true;
		}
		return false;
	}

	@Override
	public Object[] set(int index, Object[] element) {
		Object[] out = tableData.set(index, element);
		super.fireTableRowsUpdated(index, index);
		return out;
	}

	@Override
	public boolean remove(Object o) {
		int index = tableData.indexOf(o);
		if (index == -1) {
			return false;
		}
		tableData.remove(index);
		super.fireTableRowsDeleted(index, index);
		return true;
	}

	@Override
	public Object[] remove(int index) {
		Object[] out = tableData.remove(index);
		super.fireTableRowsDeleted(index, index);
		return out;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for (int i = tableData.size() - 1; i >= 0; i--) {
			if (c.contains(tableData.get(i))) {
				tableData.remove(i);
				super.fireTableRowsDeleted(i, i);
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		for (int i = tableData.size() - 1; i >= 0; i--) {
			if (!c.contains(tableData.get(i))) {
				tableData.remove(i);
				super.fireTableRowsDeleted(i, i);
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public void clear() {
		int oldSize = tableData.size();
		if (oldSize != 0) {
			tableData.clear();
			super.fireTableRowsDeleted(0, oldSize - 1);
		}
	}

	@Override
	public int size() {
		return tableData.size();
	}

	@Override
	public int getRowCount() {
		return tableData.size();
	}

	@Override
	public int getColumnCount() {
		return tableColumnNames.length;
	}

	@Override
	public int indexOf(Object o) {
		return tableData.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return tableData.lastIndexOf(o);
	}

	@Override
	public boolean contains(Object o) {
		return tableData.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return tableData.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return tableData.isEmpty();
	}

	@Override
	public Object[] get(int index) {
		return tableData.get(index);
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (row >= size()) {
			throw new IndexOutOfBoundsException("row too high: " + row + " (size=" + size() + ")");
		}
		return tableData.get(row)[col];
	}

	@Override
	public List<Object[]> subList(int fromIndex, int toIndex) {
		return tableData.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return tableData.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return tableData.toArray(a);
	}

	@Override
	public Iterator<Object[]> iterator() {
		return tableData.iterator();
	}

	@Override
	public ListIterator<Object[]> listIterator() {
		return tableData.listIterator();
	}

	@Override
	public ListIterator<Object[]> listIterator(int index) {
		return tableData.listIterator(index);
	}

	@Override
	public String getColumnName(int col) {
		return tableColumnNames[col];
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (size() == 0)
			return Object.class;
		return getValueAt(0, col).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return editable[col];
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		tableData.get(row)[col] = value;
		fireTableCellUpdated(row, col);
	}
}
