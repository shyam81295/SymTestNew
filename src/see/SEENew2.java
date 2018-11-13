package see;

import Solver.ISolver;
import Solver.SolverResult;
import Solver.Z3Solver;
import cfg.ICFG;
import cfg.ICFGBasicBlockNode;
import cfg.ICFGDecisionNode;
import cfg.ICFGNode;
import expression.*;
import mycfg.CFGBasicBlockNode;
import mycfg.CFGDecisionNode;
import program.IProgram;
import set.*;
import statement.IStatement;
import utilities.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

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

    public SETNode allPathSE (ICFG icfg, int depth) throws Exception{
        //  startnodeCFG
        ICFGBasicBlockNode startNode = icfg.getStartNode();
        //  passing empty environment & startNode
        SETNode startSETNode = this.singleStep(startNode,null);
//        System.out.println((SETBasicBlockNode)startSETNode);
//        System.out.println();
        System.out.println(startSETNode);

//        System.out.println(mSET.getStartNode().getIncomingEdge());

        LinkedList< Pair<SETNode, Integer> > setNodeQueue = new LinkedList<>();
        setNodeQueue.add(new Pair<>(startSETNode, 1));

        while(!setNodeQueue.isEmpty()){
            Pair<SETNode,Integer> pair = setNodeQueue.removeFirst();
            SETNode pairSETNode = pair.getFirst();
//            System.out.println(pairSETNode.getIncomingEdge().getTail());
//            System.out.println(pairSETNode.getIncomingEdge().getHead());
            Integer pairDepth = pair.getSecond();
//            System.out.println(pairSETNode.getId());
//            System.out.println(pairDepth);

            if(pairDepth > depth){
                continue;
            }
            ICFGNode correspondingICFGNode = pairSETNode.getCFGNode();
//            System.out.println(correspondingICFGNode);

            if(correspondingICFGNode instanceof ICFGBasicBlockNode){
//                System.out.println(((ICFGBasicBlockNode) correspondingICFGNode).getSuccessorNode());
                SETNode setNode = singleStep(((ICFGBasicBlockNode) correspondingICFGNode).getSuccessorNode(),pairSETNode);
                setNodeQueue.add(new Pair<>(setNode,pairDepth+1));
            }

            if(correspondingICFGNode instanceof ICFGDecisionNode){
                IExpression condition = ((ICFGDecisionNode) correspondingICFGNode).getCondition();

                AndExpression andExpression1 = new AndExpression(this.getSET(),pairSETNode.getPathPredicate(),condition);
                Set<IIdentifier> symVars = mSET.getVariables();
                ISolver solver = new Z3Solver(symVars, andExpression1);
                SolverResult solution = solver.solve();
                //  if satisfiable
                if(solution.getResult() == true){
                    SETNode setNode = singleStep(((ICFGDecisionNode) correspondingICFGNode).getThenSuccessorNode(),pairSETNode);
                    setNodeQueue.add(new Pair<>(setNode,pairDepth+1));
                }

                //  here we have to add "Not of the expression"
                NotExpression notExpression = new NotExpression(this.getSET(),condition);
                AndExpression andExpression2 = new AndExpression(this.getSET(),pairSETNode.getPathPredicate(),notExpression);
                Set<IIdentifier> symVars2 = mSET.getVariables();
                ISolver solver2 = new Z3Solver(symVars2, andExpression2);
                SolverResult solution2 = solver2.solve();
                //  if unsatisfiable
                if(solution2.getResult() == true){
                    SETNode setNode = singleStep(((ICFGDecisionNode) correspondingICFGNode).getElseSuccessorNode(),pairSETNode);
                    setNodeQueue.add(new Pair<>(setNode,pairDepth+1));
                }
            }
        }
        return startSETNode;
    }


    public SETNode singleStep(ICFGNode icfgNode, SETNode prevSetNode) throws Exception {
        //  for returning SETNode
        SETNode returnSETNode = null;

        if(prevSetNode == null){
            SETBasicBlockNode startSETNode = new SETBasicBlockNode(mSET,(ICFGBasicBlockNode)icfgNode);
            return startSETNode;
        }
        //  naya edge banao
        //  head is null, for just now, it will be initialised below soon.
        SETEdge newSETEdge = new SETEdge(mSET,prevSetNode,null);

        //  if icfgNode is BasicBlockNode
        if (icfgNode instanceof ICFGBasicBlockNode) {
            //  startNode hai toh, sidha wohi node return kardo
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
//        System.out.println(mSET.getEdgeSet());
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
