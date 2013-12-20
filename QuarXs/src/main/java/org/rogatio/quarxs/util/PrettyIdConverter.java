package org.rogatio.quarxs.util;

import java.util.StringTokenizer;

public class PrettyIdConverter
{

    public static String getSpace(String prettyId)
    {

        if (prettyId == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(prettyId, ".");
        
        if (st.hasMoreTokens()) {
            return st.nextToken().trim();   
        }
        
        return null;
    }

    public static String getDocumentName(String prettyId)
    {

        if (prettyId == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(prettyId, ".");
        if (st.hasMoreTokens()) {
            st.nextToken();
        } else {
            return null;
        }
        if (st.hasMoreTokens()) {
            return st.nextToken().trim();
        } 
        return null;
    }

    public static String getNodeName(String prettyId)
    {

        if (prettyId == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(prettyId, ".(");
        st.nextToken();
        st.nextToken();
        return st.nextToken().trim();
    }

    public static String replaceNodeName(String prettyid, String name)
    {
        return getSpace(prettyid) + "." + getDocumentName(prettyid) + "." + name + " (" + getGuid(prettyid) + ")";
    }

    public static String getGuid(String prettyId)
    {
        try {
            return prettyId.substring(prettyId.indexOf("(") + 1, prettyId.length() - 1).trim();
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

}
