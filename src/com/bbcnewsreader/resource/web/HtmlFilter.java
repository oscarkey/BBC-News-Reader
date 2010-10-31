package com.bbcnewsreader.resource.web;

import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.beans.*;
import org.htmlparser.util.*;

public class HtmlFilter
{
    public static void main (String args[])
    {
        TagNameFilter filter0 = new TagNameFilter ();
        filter0.setName ("div");
        HasAttributeFilter filter1 = new HasAttributeFilter ();
        filter1.setAttributeName ("class");
        filter1.setAttributeValue ("storybody");
        NodeFilter[] array0 = new NodeFilter[2];
        array0[0] = filter0;
        array0[1] = filter1;
        AndFilter filter2 = new AndFilter ();
        filter2.setPredicates (array0);
        NodeFilter[] array1 = new NodeFilter[1];
        array1[0] = filter2;
        FilterBean bean = new FilterBean ();
        bean.setFilters (array1);
        if (0 != args.length)
        {
            bean.setURL (args[0]);
            System.out.println (bean.getNodes ().toHtml ());
        }
        else
            System.out.println ("Usage: java -classpath .:htmlparser.jar filter <url>");
    }
}
