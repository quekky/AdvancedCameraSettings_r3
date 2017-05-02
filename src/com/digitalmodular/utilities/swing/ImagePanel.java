package com.digitalmodular.utilities.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * @author Mark Jeronimus
 */
// date 2011-06-20
public class ImagePanel extends JComponent
{
	public Image	image;
	public boolean	stretch;

	public ImagePanel()
	{
		this(false);
	}

	public ImagePanel(boolean stretch)
	{
		this.stretch = stretch;
	}

	public ImagePanel(BufferedImage image)
	{
		setImage(image);
	}

	public void setImage(Image im)
	{
		image = im;
		sizeChanged();
	}

	public Image getImage()
	{
		return image;
	}

	private void sizeChanged()
	{
		if (stretch)
			return;

		Dimension size = new Dimension();

		if (image != null)
		{
			size.width = image.getWidth(null);
			size.height = image.getHeight(null);
		}

		Border border = getBorder();
		if (border != null)
		{
			Insets insets = border.getBorderInsets(this);

			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;
		}

		setPreferredSize(size);
		setSize(size);
	}

	@Override
	public void setBorder(Border border)
	{
		super.setBorder(border);
		sizeChanged();
	}

	@Override
	public void paintComponent(Graphics g)
	{
		// super.paintComponent(g);

		if (image != null)
		{
			int x = 0;
			int y = 0;
			int width = getWidth();
			int height = getHeight();

			Border border = getBorder();
			if (border != null)
			{
				Insets insets = border.getBorderInsets(this);

				x = insets.left;
				y = insets.top;
				width -= insets.right + x;
				height -= insets.right + x;
			}

			if (stretch)
			{
				g.drawImage(image, x, y, width, height, this);
			}
			else
			{
				g.drawImage(image, x, y, null);
			}
		}
	}
}
