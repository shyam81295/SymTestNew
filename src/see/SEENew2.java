package see;

import cfg.ICFG;
import cfg.ICFGBasicBlockNode;
import cfg.ICFGDecisionNode;
import cfg.ICFGNode;
import expression.IExpression;
import expression.IIdentifier;
import expression.Type;
import mycfg.CFGBasicBlockNode;
import mycfg.CFGDecisionNode;
import set.*;
import statement.IStatement;

import java.util.List;

public class SEENew2 {
    private SET mSET;

    //  initialize newSEE using cfg
    public SEENew2(ICFG cfg) throws Exception {
        if (cfg != null) {
            this.mSET = new SET(cfg);
        } else {
            throw new Exception("Null CFG");
        }
    }

    public SET getSET() {
        return mSET;
    }


    public SETNode singleStep(ICFGNode icfgNode, SETNode prevSetNode) throws Exception {
        //  for returning SETNode
        SETNode returnSETNode = null;

        //  naya edge banao
        //  head is null, for just now, it will be initialised below soon.
        SETEdge newSETEdge = new SETEdge(mSET,prevSetNode,null);

        //  if icfgNode is BasicBlockNode
        if (icfgNode instanceof ICFGBasicBlockNode) {
            //  tail upar hi set ho gaya hai, head ke liye naya node banao
            returnSETNode = addNewSETBasicBlockNode(icfgNode,newSETEdge);
        }

        //  if icfgNode is DecisionNode
        else if(icfgNode instanceof ICFGDecisionNode){
            //  tail upar hi set ho gaya hai, head ke liye naya node banao
            returnSETNode = addNewSETDecisionNode(icfgNode,newSETEdge);
        }

        //  else case for escaping null pointer exception

        return returnSETNode;
    }

    //  function to add SETBasicBlockNode
    public SETBasicBlockNode addNewSETBasicBlockNode(ICFGNode newNode, SETEdge newSETEdge) throws Exception {
        SETBasicBlockNode newSETNode = new SETBasicBlockNode(mSET,
                (CFGBasicBlockNode) newNode);
        this.mSET.addBasicBlockNode(newSETNode);
        newSETEdge.setHead(newSETNode);
        newSETNode.setIncomingEdge(newSETEdge);
        this.mSET.addEdge(newSETEdge);
        this.computeStatementList(newSETNode);
        return newSETNode;
    }

    //  function to add SETDecisionNode

    public SETDecisionNode addNewSETDecisionNode(ICFGNode newNode, SETEdge newSETEdge) throws Exception {
        CFGDecisionNode decisionNode = (CFGDecisionNode) newNode;
        SETDecisionNode newSETNode = new SETDecisionNode(
                decisionNode.getCondition(), mSET, decisionNode);
        this.mSET.addDecisionNode(newSETNode);
        newSETEdge.setHead(newSETNode);
        newSETNode.setIncomingEdge(newSETEdge);
        this.mSET.addEdge(newSETEdge);
        this.computeExpression(newSETNode);
        return newSETNode;
    }

    public void computeStatementList(SETBasicBlockNode node) throws Exception {
        ICFGBasicBlockNode cfgBasicBlockNode = (ICFGBasicBlockNode) node
                .getCFGNode();
        IStatement statement = cfgBasicBlockNode.getStatement();

        IIdentifier LHS = statement.getLHS();
        IExpression RHS = statement.getRHS();

        SETExpressionVisitor visitor = new SETExpressionVisitor(node,
                LHS.getType());

        visitor.visit(RHS);

        IExpression value = null;
        //  get the symbolic expression by visiting the RHS (top of stack)
        value = visitor.getValue();

        IIdentifier var = statement.getLHS();
        //  set symbolic expression into the node
        node.setValue(var, value);
    }

    public void computeExpression(SETDecisionNode node) throws Exception {
        SETExpressionVisitor visitor = new SETExpressionVisitor(node,
                Type.BOOLEAN);
        CFGDecisionNode cfgNode = (CFGDecisionNode) node.getCFGNode();
        IExpression conditionCFG = cfgNode.getCondition();
        IExpression conditionSET = node.getCondition();
        if (conditionSET == null) {
            throw new Exception("Null Expression");
        } else {
            visitor.visit(cfgNode.getCondition());
            IExpression value = visitor.getValue();
            node.setCondition(value);
        }
    }

}
