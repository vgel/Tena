package r194.generation;

import java.util.*;

import r194.Logging;
import r194.parsing.*;

public class MacroProcessor {
	Map<String, AbstractSyntaxNode> macros = new HashMap<>();
	long uuid; //for gensym
	
	public String genSym(String prefix){
		return prefix + "_gs" + (uuid++);
	}

	public AbstractSyntaxNode expandMacros(AbstractSyntaxNode root) {
		for (AbstractSyntaxNode node : root.filter(ASTType.MACRO)) {
			String macroName = (String) node.child(0).content;
			macros.put(macroName, node);
		}

		return expandMacrosImpl(root);
	}

	private AbstractSyntaxNode expandMacrosImpl(AbstractSyntaxNode expand) {
		AbstractSyntaxNode ret = expand.clone();
		ret.setChildren(new ArrayList<AbstractSyntaxNode>());

		for (AbstractSyntaxNode node : expand) {
			if (node.type == ASTType.MACRO_CALL) {
				ret.addChildren(expandMacroCall(node));
			} else if (node.type != ASTType.MACRO) {
				ret.addChild(expandMacrosImpl(node));
			}
		}

		return ret;
	}

	private List<AbstractSyntaxNode> expandMacroCall(AbstractSyntaxNode macroCall) {
		List<AbstractSyntaxNode> ret = new ArrayList<>();
		String macroName = (String) macroCall.child(0).content;

		AbstractSyntaxNode called = macros.get(macroName);
		if (called == null)
			Logging.error("Called non-existant macro " + macroName);

		int pn = macroCall.child(1).numChildren();
		int rn = called.child(1).numChildren();
		if (called.child(1).numChildren() != macroCall.child(1).numChildren())
			Logging.error(String.format("Called %s with wrong number of arguments (called with %d, needs %d)", macroName, pn, rn));

		Map<String, AbstractSyntaxNode> args = new HashMap<>();
		for (int i = 0; i < called.child(1).numChildren(); i++) {
			args.put((String) called.child(1).child(i).content, macroCall.child(1).child(i));
		}
		Logging.debug("Call to " + macroName + " args: " + args.keySet());
		
		Map<String, String> replLabels = genUniqueLabels(called);
		for (AbstractSyntaxNode node : called.child(2)) {
			if (node.type == ASTType.MACRO_CALL)
				ret.addAll(expandMacroCall(node));
			else
				ret.add(recursiveReplace(node, args, replLabels));
		}

		return ret;
	}
	
	private Map<String, String> genUniqueLabels(AbstractSyntaxNode macro){
		Map<String, String> ret = new HashMap<>();
		genUniqueLabelsImpl(macro, ret);
		Logging.debug("labels -> " + ret);
		return ret;
	}
	
	private void genUniqueLabelsImpl(AbstractSyntaxNode node, Map<String, String> tar){
		for (AbstractSyntaxNode node1 : node){
			if (node1.type == ASTType.LABEL){
				String name = (String)node1.child(0).content;
				tar.put(name, genSym(name));
			}
			else if (node1.numChildren() > 0){
				genUniqueLabelsImpl(node1, tar);
			}
		}
	}

	private AbstractSyntaxNode recursiveReplace(AbstractSyntaxNode replace, Map<String, AbstractSyntaxNode> with, Map<String, String> replLabels) {
		AbstractSyntaxNode ret = replace.clone();
		ret.setChildren(new ArrayList<AbstractSyntaxNode>());

		for (AbstractSyntaxNode node : replace) {
			if (node.type == ASTType.IDENT) {
				ret.addChild(fixupIdents(node, with, replLabels));
			} else if (node.type == ASTType.MACRO_CALL) {
				ret.addChildren(expandMacroCall(node));
			} else {
				ret.addChild(recursiveReplace(node, with, replLabels));
			}
		}

		return ret;
	}

	private AbstractSyntaxNode fixupIdents(AbstractSyntaxNode ident, Map<String, AbstractSyntaxNode> with, Map<String, String> replLabels) {
		String identCon = (String)ident.content;
		if (with.get(identCon) != null) {
			return with.get(identCon).clone();
		}
		else if (replLabels.get(identCon) != null){
			AbstractSyntaxNode ret = ident.clone();
			ret.content = replLabels.get(identCon);
			return ret;
		}
		return ident.clone();
	}
}
