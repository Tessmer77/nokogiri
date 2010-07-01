package nokogiri;

import java.util.List;

import nokogiri.internals.NokogiriHelpers;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@JRubyClass(name="Nokogiri::XML::NodeSet")
public class XmlNodeSet extends RubyObject {
    protected RubyArray nodes;
    protected XmlDocument doc;

    public XmlNodeSet(Ruby ruby, NodeList nodes) {
        this(ruby, (RubyClass) ruby.getClassFromPath("Nokogiri::XML::NodeSet"), nodes);
    }

    public XmlNodeSet(Ruby ruby, RubyArray nodes) {
        this(ruby, (RubyClass) ruby.getClassFromPath("Nokogiri::XML::NodeSet"), nodes);
    }

    public XmlNodeSet(Ruby ruby, RubyClass rubyClass, NodeList nodes) {
        this(ruby, rubyClass, NokogiriHelpers.nodeListToRubyArray(ruby, nodes));
    }

    public XmlNodeSet(Ruby ruby, RubyClass rubyClass, RubyArray nodes){
        super(ruby, rubyClass);
        this.nodes = nodes;
        
        IRubyObject first = nodes.first();
        initializeDecorator(ruby, first);
    }
    
    public XmlNodeSet(Ruby ruby, XmlNodeSet reference){
        super(ruby, (RubyClass) ruby.getClassFromPath("Nokogiri::XML::NodeSet"));
        this.nodes = null;
        
        IRubyObject first = reference.nodes.first();
        initializeDecorator(ruby, first);
    }
    
    void setNodes(RubyArray nodes) {
        this.nodes = nodes;
    }
    
    private void initializeDecorator(Ruby ruby, IRubyObject refNode) {
        if (refNode instanceof XmlNode) {
            XmlNode n = (XmlNode)refNode;
            XmlNode owner = (XmlNode) n.document(ruby.getCurrentContext());

            if (owner != null && owner instanceof XmlDocument) {
                RuntimeHelpers.invoke(ruby.getCurrentContext(),
                                  owner, "decorate", this);
            }
        }
    }

    public XmlDocument getDocument() {
        return (XmlDocument) this.getInstanceVariable("@document");
    }

    public static IRubyObject newEmptyNodeSet(ThreadContext context) {
        Ruby ruby = context.getRuntime();
        return new XmlNodeSet(ruby,
                (RubyClass) ruby.getClassFromPath("Nokogiri::XML::NodeSet"),
                ruby.newEmptyArray());
    }

    public boolean isEmpty() {
        return this.nodes.isEmpty();
    }

    public long length() {
        return this.nodes.length().getLongValue();
    }

    public void relink_namespace(ThreadContext context) {
        List<?> n = this.nodes.getList();

        for (int i = 0; i < n.size(); i++) {
            if (n.get(i) instanceof XmlNode) {
                ((XmlNode) n.get(i)).relink_namespace(context);
            }
        }
    }

    public void setDocument(XmlDocument document) {
        this.setInstanceVariable("@document", document);
        this.doc = document;
    }

    public NodeList toNodeList(Ruby ruby) {
        return new NokogiriNodeList(ruby, this.nodes);
    }

    @JRubyMethod(name="&")
    public IRubyObject and(ThreadContext context, IRubyObject nodeSet){
        return newXmlNodeSet(context, (RubyArray) nodes.op_and(asXmlNodeSet(context, nodeSet).nodes));
    }

    @JRubyMethod
    public IRubyObject delete(ThreadContext context, IRubyObject node_or_namespace){
        return nodes.delete(context, asXmlNodeOrNamespace(context, node_or_namespace), Block.NULL_BLOCK);
    }

    @JRubyMethod
    public IRubyObject dup(ThreadContext context){
        return newXmlNodeSet(context, nodes.aryDup());
    }

    @JRubyMethod(name = "include?")
    public IRubyObject include_p(ThreadContext context, IRubyObject node_or_namespace){
        return nodes.include_p(context, asXmlNodeOrNamespace(context, node_or_namespace));
    }

    @JRubyMethod(name = {"length", "size"})
    public IRubyObject length(ThreadContext context) {
        return nodes.length();
    }

    @JRubyMethod(name="-")
    public IRubyObject op_diff(ThreadContext context, IRubyObject nodeSet){
        XmlNodeSet xmlNodeSet = newXmlNodeSet(context, this);
        xmlNodeSet.setNodes((RubyArray) nodes.op_diff(asXmlNodeSet(context, nodeSet).nodes));
        return xmlNodeSet;
    }

    @JRubyMethod(name={"|", "+"})
    public IRubyObject op_or(ThreadContext context, IRubyObject nodeSet){
        return newXmlNodeSet(context, (RubyArray) nodes.op_or(asXmlNodeSet(context, nodeSet).nodes));
    }

    @JRubyMethod(name = {"push", "<<"})
    public IRubyObject push(ThreadContext context, IRubyObject node_or_namespace) {
        nodes.append(asXmlNodeOrNamespace(context, node_or_namespace));
        return this;
    }

    @JRubyMethod(name={"[]", "slice"})
    public IRubyObject slice(ThreadContext context, IRubyObject indexOrRange){
        IRubyObject result;
        if (context.getRuntime().is1_9()) {
            result = nodes.aref19(indexOrRange);
        } else {
            result = nodes.aref(indexOrRange);
        }
        if (result instanceof RubyArray) {
            return newXmlNodeSet(context, (RubyArray)result);
        } else {
            return result;
        }
    }

    @JRubyMethod(name={"[]", "slice"})
    public IRubyObject slice(ThreadContext context, IRubyObject start, IRubyObject length){
        IRubyObject result;
        if (context.getRuntime().is1_9()) {
            result = nodes.aref19(start, length);
        } else {
            result = nodes.aref(start, length);
        }
        if (result instanceof RubyArray) return newXmlNodeSet(context, (RubyArray)result);
        else return context.getRuntime().getNil();
    }

    @JRubyMethod(name = {"to_a", "to_ary"})
    public IRubyObject to_a(ThreadContext context){
       return nodes;
    }

    @JRubyMethod(name = {"unlink", "remove"})
    public IRubyObject unlink(ThreadContext context){
        IRubyObject[] arr = this.nodes.toJavaArrayUnsafe();
        long length = arr.length;
        for (int i = 0; i < length; i++) {
            if (arr[i] instanceof XmlNode) {
                ((XmlNode) arr[i] ).unlink(context);
            }
        }
        return this;
    }

    private XmlNodeSet newXmlNodeSet(ThreadContext context, RubyArray array) {
        XmlNodeSet result = new XmlNodeSet(context.getRuntime(), nodeSetClass(context), array);
        result.setDocument(this.getDocument());
        return result;
    }
    
    private XmlNodeSet newXmlNodeSet(ThreadContext context, XmlNodeSet reference) {
        XmlNodeSet result = new XmlNodeSet(context.getRuntime(), reference);
        result.setDocument(this.getDocument());
        return result;
    }

    private RubyClass nodeSetClass(ThreadContext context) {
        return (RubyClass) context.getRuntime().getClassFromPath("Nokogiri::XML::NodeSet");
    }

    private XmlNode asXmlNode(ThreadContext context, IRubyObject possibleNode) {
        if(!(possibleNode instanceof XmlNode)) {
            throw context.getRuntime().newArgumentError("node must be a Nokogiri::XML::Node");
        }

        return (XmlNode) possibleNode;
    }
    
    private IRubyObject asXmlNodeOrNamespace(ThreadContext context, IRubyObject possibleNode) {
        if (possibleNode instanceof XmlNode || possibleNode instanceof XmlNamespace) {
            return possibleNode;
        } else {
            throw context.getRuntime().newArgumentError("node must be a Nokogiri::XML::Node or Nokogiri::XML::Namespace");
        }
    }

    private XmlNodeSet asXmlNodeSet(ThreadContext context, IRubyObject possibleNodeSet) {
//        if(!(possibleNodeSet instanceof XmlNodeSet)) {
        if(!RuntimeHelpers.invoke(context, possibleNodeSet, "is_a?",
                context.getRuntime().getClassFromPath("Nokogiri::XML::NodeSet")).isTrue()) {
            throw context.getRuntime().newArgumentError("node must be a Nokogiri::XML::NodeSet");
        }
        return (XmlNodeSet) possibleNodeSet;
    }

    class NokogiriNodeList implements NodeList{

        private final RubyArray nodes;
        private final Ruby ruby;

        public NokogiriNodeList(Ruby ruby, RubyArray nodes) {
            this.nodes = nodes;
            this.ruby = ruby;
        }

        public Node item(int i) {
            return XmlNode.getNodeFromXmlNode(ruby.getCurrentContext(),
                    this.nodes.aref(ruby.newFixnum(i)));
        }

        public int getLength() {
            return this.nodes.getLength();
        }

    }
}