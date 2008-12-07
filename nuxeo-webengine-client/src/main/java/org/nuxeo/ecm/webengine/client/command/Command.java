/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.ecm.webengine.client.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import org.nuxeo.ecm.webengine.client.cmds.Help;
import org.nuxeo.ecm.webengine.client.http.HttpClient;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public abstract class Command {

    protected CommandSyntax syntax;
    protected String[] aliases;
    protected String synopsis;
    protected URL url;


    protected Command() {

    }

    public String getName() {
        return aliases[0];
    }

    /**
     * @return the aliases.
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * @return the synopsis.
     */
    public String getSynopsis() {
        return synopsis;
    }

    /**
     * @return the syntax.
     */
    public CommandSyntax getSyntax() {
        return syntax;
    }

    /**
     * @return the help.
     */
    public String getHelp() {
        if (url == null) {
            url = getClass().getResource("/META-INF/help/"+getName()+".help");
            if (url == null) {
                return "N/A";
            }
        }
        InputStream in = null;
        StringWriter out = new StringWriter();
        try {
            in = url.openStream();
            byte[] bytes = new byte[4096*2];
            int r = in.read(bytes);
            while (r > -1) {
                if (r > 0) {
                    out.write(new String(bytes, 0, r));
                }
                r = in.read(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (in != null) in.close(); } catch (IOException e) {}
        }
        return out.toString();
    }


    /**
     * @return the url.
     */
    public URL getUrl() {
        return url;
    }


    public void print(String str) {
        System.out.print(str);
    }

    public void println(String str) {
        System.out.println(str);
    }

    public boolean isLocal() {
        return true;
    }

    public abstract void run(HttpClient client, CommandLine cmdLine) throws Exception;


    public static void main(String[] args) {
        Help help = new Help();

        //String r = "org/nuxeo/ecm/webengine/client/builtin/HelpCommand.help";
        URL url = Help.class.getResource("HelpCommand.help");
        System.out.println(url);
        url = Help.class.getResource("ExitCommand.help");
        System.out.println(url);

    }

}
