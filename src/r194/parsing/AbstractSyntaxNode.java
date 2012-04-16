package r194.parsing;

import java.util.*;

public class AbstractSyntaxNode implements Iterable<AbstractSyntaxNode>, Cloneable {
	private List<AbstractSyntaxNode> children;
	public ASTType type;
	public Object content;
	public AbstractSyntaxNode parent;
	public int pos;
	
	public AbstractSyntaxNode(ASTType type, Object content, AbstractSyntaxNode parent) {
		children = new ArrayList<>();
		this.type = type;
		this.content = content;
		this.parent = parent;
	}
	
	public void addChild(AbstractSyntaxNode node){
		node.pos = children.size();
		node.parent = this;
		children.add(node);
	}
	
	public AbstractSyntaxNode addChild(ASTType type, Object content) {
		AbstractSyntaxNode node = new AbstractSyntaxNode(type, content, this);
		addChild(node);
		return node;
	}
	
	public void addChildren(Collection<? extends AbstractSyntaxNode> nodes){
		children.addAll(nodes);
	}
	
	public List<AbstractSyntaxNode> getChildren(){
		return children;
	}
	
	public void setChildren(List<AbstractSyntaxNode> children) {
		this.children = children;
	}
	
	public int numChildren(){
		return children.size();
	}
	
	@Override
	public Iterator<AbstractSyntaxNode> iterator() {
		return children.iterator();
	}
	
	@Override
	public AbstractSyntaxNode clone() {
		AbstractSyntaxNode ret = new AbstractSyntaxNode(type, content, parent);
		
		List<AbstractSyntaxNode> newChildren = new ArrayList<>();
		for (AbstractSyntaxNode node : children)
			newChildren.add(node.clone());
		
		ret.setChildren(newChildren);
		ret.pos = pos;
		return ret;
	}
	
	public AbstractSyntaxNode child(int num){
		return children.get(num);
	}
	
	public AbstractSyntaxNode head(){
		return children.get(0);
	}
	
	public List<AbstractSyntaxNode> filter(ASTType type){
		List<AbstractSyntaxNode> ret = new ArrayList<>();
		for (AbstractSyntaxNode child : children){
			if (child.type == type) ret.add(child);
		}
		return ret;
	}
	
	List<AbstractSyntaxNode> getSiblings() {
		if (parent == null) return null;
		return parent.children;
	}
	
	public AbstractSyntaxNode leftSibling(){
		List<AbstractSyntaxNode> siblings = getSiblings();
		if (siblings == null) return null;
		if (pos <= 0) return null;
		return siblings.get(pos - 1);
	}
	
	public AbstractSyntaxNode rightSibling(){
		List<AbstractSyntaxNode> siblings = getSiblings();
		if (siblings == null) return null;
		if (pos >= siblings.size() - 1) return null;
		return siblings.get(pos + 1);
	}

	int distanceFromRoot() {
		int dist = 0;
		AbstractSyntaxNode n = this;
		while (n.parent != null) {
			dist++;
			n = n.parent;
		}
		return dist;
	}

	@Override
	public String toString() {
		int distRoot = distanceFromRoot();
		String ret = "" + type + ", " + children.size() + ", " + content + ", " + (parent == null) + ", " + (getSiblings() != null ? getSiblings().size() : -1) + "\n";
		for (AbstractSyntaxNode child : children){
			for (int i = 0; i <= distRoot; i++){
				ret += "\t";
			}
			ret += child.toString();
		}
		return ret;
	}
}