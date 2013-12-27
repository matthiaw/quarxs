/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.rogatio.quarxs.view;

import org.xwiki.properties.annotation.PropertyDescription;

public class GraphMacroParameters
{
    private String layout;

    private String width;

    private String height;

    private String menu;

    private String data;

    private String overwrite;

    public String getOverwrite()
    {
        return overwrite;
    }

    @PropertyDescription("Overwrite Code")
    public void setOverwrite(String overwrite)
    {
        this.overwrite = overwrite;
    }

    public String getLayout()
    {
        return this.layout;
    }

    @PropertyDescription("Layout")
    public void setLayout(String layout)
    {
        this.layout = layout;
    }

    public String getWidth()
    {
        return width;
    }

    public String getHeight()
    {
        return height;
    }

    @PropertyDescription("Height")
    public void setHeight(String height)
    {
        this.height = height;
    }

    public String getData()
    {
        return data;
    }

    public String getMenu()
    {
        return menu;
    }

    @PropertyDescription("Show Data")
    public void setData(String data)
    {
        this.data = data;
    }

    @PropertyDescription("Show Menu")
    public void setMenu(String menu)
    {
        this.menu = menu;
    }

    @PropertyDescription("Width")
    public void setWidth(String width)
    {
        this.width = width;
    }

}
