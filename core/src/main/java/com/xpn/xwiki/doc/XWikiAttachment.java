/*
 * Copyright 2006, XpertNet SARL, and individual contributors as indicated
 * by the contributors.txt.
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
 *
 * @author ludovic
 * @author jeremi
 * @author sdumitriu
 */

package com.xpn.xwiki.doc;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import org.apache.commons.codec.binary.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.ObjectNotFoundException;
import org.suigeneris.jrcs.rcs.Archive;
import org.suigeneris.jrcs.rcs.Node;
import org.suigeneris.jrcs.rcs.Version;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XWikiAttachment {
    private XWikiDocument doc;

    private int filesize;
    private String filename;
    private String author;
    private Version version;
    private String comment;
    private Date date;
    // Meta Data Archive
    private Archive metaArchive;

    private XWikiAttachmentContent attachment_content;
    private XWikiAttachmentArchive attachment_archive;
    private boolean isMetaDataDirty = false;

    public XWikiAttachment(XWikiDocument doc, String filename) {
        this();
        setDoc(doc);
        setFilename(filename);
    }

    public XWikiAttachment() {
        filesize = 0;
        filename = "";
        author = "";
        comment = "";
        date = new Date();
    }

    public long getId() {
        if (doc==null)
         return filename.hashCode();
        else
         return (doc.getFullName() + "/" + filename).hashCode();
    }

    public void setDocId(long id) {
    }

    public long getDocId() {
        return doc.getId();
    }

    public void setId(long id) {
    }

    public Object clone() {
        XWikiAttachment attachment = null;
        try {
            attachment = (XWikiAttachment) getClass().newInstance();
        } catch (Exception e) {
            // This should not happen
        }

        attachment.setAuthor(getAuthor());
        attachment.setComment(getComment());
        attachment.setDate(getDate());
        attachment.setDoc(getDoc());
        attachment.setFilename(getFilename());
        attachment.setFilesize(getFilesize());
        attachment.setRCSVersion(getRCSVersion());
        if (getAttachment_content()!=null) {
         attachment.setAttachment_content((XWikiAttachmentContent)getAttachment_content().clone());
         attachment.getAttachment_content().setAttachment(attachment);
        }
        if (getAttachment_archive()!=null) {
         attachment.setAttachment_archive((XWikiAttachmentArchive)getAttachment_archive().clone());
         attachment.getAttachment_archive().setAttachment(attachment);
        }
        return attachment;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        if (filesize != this.filesize) {
                setMetaDataDirty(true);
            }
        this.filesize = filesize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        filename = filename.replaceAll("\\+"," ");
        if (!filename.equals(this.filename)) {
                setMetaDataDirty(true);
                this.filename = filename;
            }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (!author.equals(this.author)) {
                setMetaDataDirty(true);
            }
        this.author = author;
    }

    public String getVersion() {
        return getRCSVersion().toString();
    }

    public void setVersion(String version) {
        this.version = new Version(version);
    }

    public Version getRCSVersion() {
        if (version == null) {
            version = new Version("1.1");
        }
        return version;
    }

    public void setRCSVersion(Version version) {
        this.version = version;
    }

    public String getComment() {
        return comment != null ? comment : "";
    }

    public void setComment(String comment) {
        if (!getComment().equals(comment)) {
            setMetaDataDirty(true);
        }
        this.comment = comment;
    }

    public XWikiDocument getDoc() {
        return doc;
    }

    public void setDoc(XWikiDocument doc) {
        this.doc = doc;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        // Make sure we drop milliseconds for consistency with the database
        if (date!=null)
         date.setTime((date.getTime()/1000) * 1000);
        this.date = date;
    }


    public boolean isContentDirty() {
        if (attachment_content==null)
            return false;
        else
            return attachment_content.isContentDirty();
    }

    public void incrementVersion() {
        if (version==null)
            version = new Version("1.1");
        else {
            version = version.next();
        }
    }

    public boolean isMetaDataDirty() {
        return isMetaDataDirty;
    }

    public void setMetaDataDirty(boolean metaDataDirty) {
        isMetaDataDirty = metaDataDirty;
    }

    public String toStringXML(boolean bWithAttachmentContent, boolean bWithVersions, XWikiContext context) throws XWikiException {
        // implement
        Element ele = toXML(bWithAttachmentContent,bWithVersions,context);

        Document doc = new DOMDocument();
        doc.setRootElement(ele);
        OutputFormat outputFormat = new OutputFormat("", true);
        if ((context==null)||(context.getWiki()==null))
            outputFormat.setEncoding("UTF-8");
        else
            outputFormat.setEncoding(context.getWiki().getEncoding());
        StringWriter out = new StringWriter();
        XMLWriter writer = new XMLWriter( out, outputFormat );
        try {
            writer.write(doc);
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Element toXML(XWikiContext context) throws XWikiException {
        return toXML(false, false, context);
    }

    public Element toXML(boolean bWithAttachmentContent, boolean bWithVersions, XWikiContext context) throws XWikiException {
        Element docel = new DOMElement("attachment");
        Element el = new DOMElement("filename");
        el.addText(getFilename());
        docel.add(el);

        el = new DOMElement("filesize");
        el.addText("" + getFilesize());
        docel.add(el);

        el = new DOMElement("author");
        el.addText(getAuthor());
        docel.add(el);

        long d = getDate().getTime();
        el = new DOMElement("date");
        el.addText("" + d);
        docel.add(el);

        el = new DOMElement("version");
        el.addText(getVersion());
        docel.add(el);

        el = new DOMElement("comment");
        el.addText(getComment());
        docel.add(el);

        if (bWithAttachmentContent) {
            el = new DOMElement("content");
            // We need to make sure content is loaded
            loadContent(context);
            XWikiAttachmentContent acontent = getAttachment_content();
            if (acontent!=null) {
             byte[] bcontent = getAttachment_content().getContent();
             String content = new String(Base64.encodeBase64(bcontent));
             el.addText(content);
            } else {
                el.addText("");
            }
            docel.add(el);
        }

        if (bWithVersions) {
            // We need to make sure content is loaded
            loadArchive(context);
            XWikiAttachmentArchive aarchive = getAttachment_archive();
            if (aarchive!=null) {
                el = new DOMElement("versions");
                try {
                    el.addText(new String(aarchive.getArchive()));
                } catch (XWikiException e) {
                    return null;
                }
                docel.add(el);
            }
        }
        return docel;
    }


    public void fromXML(String data) throws XWikiException {
        SAXReader reader = new SAXReader();
        Document domdoc = null;
        try {
            StringReader in = new StringReader(data);
            domdoc = reader.read(in);
        } catch (DocumentException e) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_DOC, XWikiException.ERROR_DOC_XML_PARSING, "Error parsing xml", e, null);
        }
        Element docel = domdoc.getRootElement();
        fromXML(docel);
    }

    public void fromXML(Element docel) throws XWikiException {
        setFilename(docel.element("filename").getText());
        setFilesize(Integer.parseInt(docel.element("filesize").getText()));
        setAuthor(docel.element("author").getText());
        setVersion(docel.element("version").getText());
        setComment(docel.element("comment").getText());

        String sdate = docel.element("date").getText();
        Date date = new Date(Long.parseLong(sdate));
        setDate(date);

        Element contentel = docel.element("content");
        if (contentel!=null) {
            String base64content = contentel.getText();
            byte[] content = Base64.decodeBase64(base64content.getBytes());
            setContent(content);
        }
        Element archiveel = docel.element("versions");
        if (archiveel!=null) {
            String archive = archiveel.getText();
            setArchive(archive);
        }

    }

    public XWikiAttachmentContent getAttachment_content() {
        return attachment_content;
    }

    public void setAttachment_content(XWikiAttachmentContent attachment_content) {
        this.attachment_content = attachment_content;
    }

    public XWikiAttachmentArchive getAttachment_archive() {
        return attachment_archive;
    }

    public void setAttachment_archive(XWikiAttachmentArchive attachment_archive) {
        this.attachment_archive = attachment_archive;
    }

    public byte[] getContent(XWikiContext context) throws XWikiException {
        if (attachment_content==null) {
            doc.loadAttachmentContent(this, context);
        }

        return attachment_content.getContent();
    }

    public Archive getArchive() {
        if (attachment_archive==null)
            return null;
        else
            return attachment_archive.getRCSArchive();
    }

    public void setArchive(Archive archive) {
        if (attachment_archive==null) {
            attachment_archive = new XWikiAttachmentArchive();
            attachment_archive.setAttachment(this);
        }
        attachment_archive.setRCSArchive(archive);
    }

    public void setArchive(String data) throws XWikiException {
        if (attachment_archive==null) {
            attachment_archive = new XWikiAttachmentArchive();
            attachment_archive.setAttachment(this);
        }
        attachment_archive.setArchive(data.getBytes());
    }


    public synchronized Version[] getVersions() {
        Node[] nodes = getArchive().changeLog();
        Version[] versions = new Version[nodes.length];
        for (int i=0;i<nodes.length;i++) {
            versions[i] = nodes[i].getVersion();
        }
        return versions;
    }

    // We assume versions go from 1.1 to the current one
    // This allows not to read the full archive file
    public synchronized List getVersionList() throws XWikiException {
            List list = new ArrayList();
            Version v = new Version("1.1");
            while (true) {
                list.add(v);
                if (v.toString().equals(version.toString()))
                    break;
                v.next();
            }
            return list;
    }

    public void setContent(byte[] data) {
        if (attachment_content==null) {
            attachment_content = new XWikiAttachmentContent();
            attachment_content.setAttachment(this);
        }
        attachment_content.setContent(data);
    }

    public void loadContent(XWikiContext context) throws XWikiException {
        if (attachment_content==null)
            context.getWiki().getAttachmentStore().loadAttachmentContent(this, context, true);
    }

    public void loadArchive(XWikiContext context) throws XWikiException {
        if (attachment_archive==null)
            context.getWiki().getAttachmentStore().loadAttachmentArchive(this, context, true);
    }

    public void updateContentArchive(XWikiContext context) throws XWikiException {
        if (attachment_content == null)
         return;

        if (attachment_archive==null) {
             try {
               context.getWiki().getAttachmentStore().loadAttachmentArchive(this, context, true);
             } catch (XWikiException e) {
                if (!(e.getException() instanceof ObjectNotFoundException))
                    throw e;
             }
        }

        if (attachment_archive==null) {
            attachment_archive = new XWikiAttachmentArchive();
            attachment_archive.setAttachment(this);
        }

        attachment_archive.updateArchive(getContent(context), context);
    }

    public String getMimeType(XWikiContext context) {
        // Choose the right content type
        String mimetype = context.getEngineContext().getMimeType(getFilename().toLowerCase());
        if (mimetype!=null)
            return mimetype;
        else
            return "application/octet-stream";
    }

    public boolean isImage(XWikiContext context) {
        String contenttype = getMimeType(context);
        if (contenttype.startsWith("image/"))
            return true;
        else
            return false;
    }

    public XWikiAttachment getAttachmentRevision(String rev, XWikiContext context) throws XWikiException {

        try {
            context.getWiki().getAttachmentStore().loadAttachmentArchive(this, context, true);

            Archive archive = getArchive();

            if (archive==null)
             return null;

            Version v = archive.getRevisionVersion(rev);
            Object[] lines = archive.getRevision(v);
            StringBuffer content = new StringBuffer();
            for (int i=0;i<lines.length;i++) {
                String line = lines[i].toString();
                content.append(line);
                if (i!=lines.length-1)
                    content.append("\n");
            }

            String scontent = content.toString();
            XWikiAttachment revattach = new XWikiAttachment();
            revattach.fromXML(scontent);
            revattach.setDoc(getDoc());
            revattach.setVersion(rev);
            return revattach;
        }    catch (Exception e) {
            Object[] args = { getFilename() };
            throw new XWikiException( XWikiException.MODULE_XWIKI_STORE, XWikiException.ERROR_XWIKI_STORE_ATTACHMENT_ARCHIVEFORMAT,
                    "Exception while manipulating the archive for file {0}", e, args);
        }
    }

}


