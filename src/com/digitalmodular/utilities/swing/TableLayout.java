package com.digitalmodular.utilities.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A Layout Manager which creates a basic table of components. The widths of the cells are specified as a ratio array of <code>int</code>. The
 * relative widths of the rendered columns will always follow these ratios.<br>
 *
 * The preferred widths of the columns will be either the sum of the columns with configured widths as pixels, or the configured ratios
 * stretched equally in such a way that no single component is narrower than it's own preferred width, whichever is larger, added to the sum of
 * the gap widths. When the width of the parent container is larger, the cells are simply stretched linearly according to the configured ratios.
 *
 * A special case is when a ratio is specified as zero. In these cases, each column specified as zero will get such a preferred width that no
 * single component inside that column is narrower than it's own preferred width. These columns do stretch, however, when the width of the
 * parent container is larger than the preferred width, everything is stretched linearly.
 *
 * For example, a <code>TableLayout</code> with column widths <code>{5, 2}</code> filled with equal components with a width of 100 pixels will
 * generate a layout with preferred column widths of 250 and 100 pixels (ratio 5:2). Setting this layout to a container with an absolute width
 * of 700 pixels will force the table columns to 500 and 200 pixels wide.<br>
 *
 * Another example. A <code>TableLayout</code> with column widths <code>{0, 200, 0, 120}</code> filled with different components for each column
 * with relative widths of 30, 100, 50 and 100 pixels will generate a layout with preferred column widths of 30, 200, 50 and 120 pixels (ratio
 * x:200:x:120). Setting this layout to a container with an absolute width of 800 pixels will force the table columns to 60, 400, 100 and 240
 * pixels wide.
 *
 * @author Mark Jeronimus
 * @see LayoutManager
 * @see Component#getPreferredSize() date 2006/11/24
 */
public class TableLayout implements LayoutManager, Serializable
{
	private int		hgap;
	private int		vgap;
	private int[]	columns;
	private int[]	colWidths;

	private int		numColumns;
	private int[]	rowHeights;
	private int		widestColumn;

	/**
	 * Create a TableLayout with specified column width ratios and gap sizes.
	 *
	 * @param hgap
	 *        the horizontal space between all columns, in pixels
	 * @param vgap
	 *        the vertical space between all rows, in pixels
	 * @param columns
	 *        the column-width ratios
	 * @throws IllegalArgumentException
	 *         when the array is null, the array length is zero or one of the array elements is negative
		 */
	public TableLayout(int hgap, int vgap, int... columns)
	{
		setColumns(columns);
		setHgap(hgap);
		setVgap(vgap);
	}

	/**
	 * Set or change the column width ratios.
	 *
	 * @param columns
	 *        the column width ratios
	 * @throws IllegalArgumentException
	 *         when the array is null, the array length is zero or one of the array elements is negative
		 */
	public void setColumns(int... columns)
	{
		if (columns == null || columns.length == 0)
			throw new IllegalArgumentException("No columns specified");
		this.columns = columns;
		numColumns = this.columns.length;

		int widestColumnSize = -1;
		widestColumn = 0;
		for (int i = 0; i < numColumns; i++)
		{
			int w = this.columns[i];
			if (w < 0)
				throw new IllegalArgumentException("Negative column width specified at index " + i);

			if (w > 0 && widestColumnSize < w)
			{
				widestColumnSize = w;
				widestColumn = i;
			}
		}

		colWidths = new int[this.columns.length];
	}

	/**
	 * Returns the column width ratios. The returned array is a direct reference to the contained column array, so care must be taken not to
	 * alter any elements to illegal values (negative values).
	 *
	 * @return the column width ratios
	 */
	public int[] getColumnns()
	{
		return columns;
	}

	/**
	 * Set or change the horizontal space between all columns, in pixels
	 *
	 * @param hgap
	 *        the horizontal space between all columns, in pixels
	 */
	public void setHgap(int hgap)
	{
		if (hgap < 0)
			throw new IllegalArgumentException("Negative hgap specified");
		this.hgap = hgap;
	}

	/**
	 * Get the horizontal space between all columns, in pixels
	 *
	 * @return the horizontal space between all columns, in pixels
	 */
	public int getHgap()
	{
		return hgap;
	}

	/**
	 * Set or change the vertical space between all rows, in pixels
	 *
	 * @param vgap
	 *        the vertical space between all rows, in pixels
	 */
	public void setVgap(int vgap)
	{
		if (vgap < 0)
			throw new IllegalArgumentException("Negative vgap specified");
		this.vgap = vgap;
	}

	/**
	 * Get the vertical space between all rows, in pixels
	 *
	 * @return the vertical space between all rows, in pixels
	 */
	public int getVgap()
	{
		return vgap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLayoutComponent(String name, Component comp)
	{}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLayoutComponent(Component comp)
	{}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		synchronized (parent.getTreeLock())
		{
			Insets insets = parent.getInsets();
			int components = parent.getComponentCount();
			int numRows = (components + numColumns - 1) / numColumns;

			rowHeights = new int[numRows];
			Arrays.fill(rowHeights, 0);

			int narrowestColumn = widestColumn;
			int narrowestSize = columns[widestColumn];

			int component = 0;
			for (int y = 0; y < numRows; y++)
			{
				for (int x = 0; x < numColumns; x++)
				{
					Component comp = parent.getComponent(component++);
					Dimension d = comp.getPreferredSize();

					if (columns[x] == 0)
					{
						if (colWidths[x] < d.width)
							colWidths[x] = d.width;
					}
					else if (d.width > columns[x] * narrowestSize / columns[narrowestColumn])
					{
						if (columns[x] > 0)
							narrowestColumn = x;
						narrowestSize = d.width;
					}

					if (rowHeights[y] < d.height)
						rowHeights[y] = d.height;

					if (component == components)
						break;
				}
			}
			int totalWidth = insets.left + insets.right + (numColumns - 1) * hgap;
			for (int x = 0; x < numColumns; x++)
			{
				if (columns[x] != 0)
					colWidths[x] = columns[x] * narrowestSize / columns[narrowestColumn];

				totalWidth += colWidths[x];
			}
			int totalHeight = insets.top + insets.bottom + (numRows - 1) * vgap;
			for (int y = 0; y < numRows; y++)
				totalHeight += rowHeights[y];
			return new Dimension(totalWidth, totalHeight);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		synchronized (parent.getTreeLock())
		{
			Insets insets = parent.getInsets();
			int components = parent.getComponentCount();
			int numRows = (components + numColumns - 1) / numColumns;

			int[] colWidths = new int[numColumns];
			System.arraycopy(columns, 0, colWidths, 0, numColumns);
			int[] rowHeights = new int[numRows];
			Arrays.fill(rowHeights, 0);

			int component = 0;
			for (int y = 0; y < numRows; y++)
			{
				for (int x = 0; x < numColumns; x++)
				{
					Component comp = parent.getComponent(component++);
					Dimension d = comp.getMinimumSize();
					if (colWidths[x] < d.width)
						colWidths[x] = d.width;
					if (rowHeights[y] < d.height)
						rowHeights[y] = d.height;
					if (component == components)
						break;
				}
			}
			int totalWidth = insets.left + insets.right + (numColumns - 1) * hgap;
			for (int x = 0; x < numColumns; x++)
				totalWidth += colWidths[x];
			int totalHeight = insets.top + insets.bottom + (numRows - 1) * vgap;
			for (int y = 0; y < numRows; y++)
				totalHeight += rowHeights[y];
			return new Dimension(totalWidth, totalHeight);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void layoutContainer(Container parent)
	{
		synchronized (parent.getTreeLock())
		{
			Insets insets = parent.getInsets();
			int components = parent.getComponentCount();

			int numRows = (components + numColumns - 1) / numColumns;

			Dimension preferredSize = preferredLayoutSize(parent);
			int width = parent.getWidth() - (insets.left + insets.right + (numColumns - 1) * hgap);
			int height = parent.getHeight() - (insets.top + insets.bottom + (numRows - 1) * vgap);
			int preferredWidth = preferredSize.width - (insets.left + insets.right + (numColumns - 1) * hgap);
			int preferredHeight = preferredSize.height - (insets.top + insets.bottom + (numRows - 1) * vgap);

			int[] currentColWidths = new int[numColumns];
			for (int x = 0; x < numColumns; x++)
				currentColWidths[x] = colWidths[x] * width / preferredWidth;
			int[] currentRowHeights = new int[numRows];
			for (int y = 0; y < numRows; y++)
				currentRowHeights[y] = rowHeights[y] * height / preferredHeight;

			int component = 0;
			int yPos = insets.top;
			for (int y = 0; y < numRows; y++)
			{
				int xPos = insets.left;
				for (int x = 0; x < numColumns; x++)
				{
					parent.getComponent(component++).setBounds(xPos, yPos, currentColWidths[x], currentRowHeights[y]);
					if (component == components)
						break;

					xPos += currentColWidths[x] + hgap;
				}
				yPos += currentRowHeights[y] + vgap;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return super.getClass().getSimpleName() + "[columns=" + columns + ", hgap=" + hgap + ", vgap=" + vgap + "]";
	}
}
