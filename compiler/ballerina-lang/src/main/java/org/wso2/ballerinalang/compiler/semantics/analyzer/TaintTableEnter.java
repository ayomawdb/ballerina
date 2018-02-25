package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeofExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;

/**
 * Responsible of following operations related to taint checking:
 *
 * (*) Performing data flow analysis and propagating tainted status.
 * (*) Validating if invocations use tainted values with secure parameters
 */
public class TaintTableEnter extends BLangNodeVisitor {
    private static final CompilerContext.Key<TaintTableEnter> TAINT_TABLE_ENTER_KEY =
            new CompilerContext.Key<>();

    private BLangPackage pkgNode;
    private Names names;
    private SymbolResolver symResolver;
    private SymbolEnter symbolEnter;
    private DiagnosticLog dlog;
    private SymbolEnv env;

    private List<Boolean> taintedStatus;
    private boolean nonOverridingContext;

    private boolean initialGenerationComplete;
    private boolean errorConditionObserved;
    private boolean stopAnalysis;
    private BlockedNode blockedNode;

    private Map<BlockingNode, List<BlockedNode>> blockedList = new HashMap<>();

    public static TaintTableEnter getInstance(CompilerContext context) {
        TaintTableEnter taintTableEnter = context.get(TAINT_TABLE_ENTER_KEY);
        if (taintTableEnter == null) {
            taintTableEnter = new TaintTableEnter(context);
        }
        return taintTableEnter;
    }

    public TaintTableEnter(CompilerContext context) {
        context.put(TAINT_TABLE_ENTER_KEY, this);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        this.pkgNode = pkgNode;
        pkgNode.accept(this);
        return pkgNode;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TAINT_TABLE_ENTER)) {
            return;
        }
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        this.env = pkgEnv;
        pkgNode.imports.forEach(impPkgNode -> impPkgNode.accept(this));

        initialGenerationComplete = false;
        pkgNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
        initialGenerationComplete = true;

        int blockedCount = -1;
        while (blockedList.size() > 0) {
            Map<BlockingNode, List<BlockedNode>> clearedBlockedList = new HashMap<>();
            int iterationBlockedCount = 0;
            for (BlockingNode nodeId : blockedList.keySet()) {
                List<BlockedNode> invokableNodeList = blockedList.get(nodeId);
                List<BlockedNode> clearedInvokableNodeList = new ArrayList<>();
                for (BlockedNode node : invokableNodeList) {
                    node.invokableNode.accept(this);
                    if (node.invokableNode.symbol.taintTable == null) {
                        clearedInvokableNodeList.add(node);
                        iterationBlockedCount++;
                    }
                }
                if (clearedInvokableNodeList.size() > 0) {
                    clearedBlockedList.put(nodeId, clearedInvokableNodeList);
                }
            }
            if (blockedCount == iterationBlockedCount) {
                // No block has been resolved. This should mean there is a loop of dependencies.
                for (BlockingNode nodeId : clearedBlockedList.keySet()) {
                    List<BlockedNode> invokableNodeList = clearedBlockedList.get(nodeId);
                    for (BlockedNode node : invokableNodeList) {
                        attachTaintTableBasedOnFlags(node.invokableNode);
                        this.dlog.warning(node.blockedPos, DiagnosticCode.UNABLE_TO_PERFORM_TAINT_CHECKING_IN_LOOP,
                                node.invokableNode.name.value);
                    }
                }
                break;
            } else {
                blockedCount = iterationBlockedCount;
            }
            blockedList = clearedBlockedList;
        }

        pkgNode.completedPhases.add(CompilerPhase.TAINT_TABLE_ENTER);
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }
        this.env = pkgEnv;
        pkgEnv.node.accept(this);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI.accept(this);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        visitInvocable(funcNode, funcEnv);
    }

    private void visitInvocable(BLangInvokableNode invNode, SymbolEnv symbolEnv) {
        if (invNode.symbol.taintTable == null) {
            if (invNode.params.size() > 0 && invNode.retParams.size() > 0) {
                if (Symbols.isNative(invNode.symbol)) {
                    attachTaintTableBasedOnFlags(invNode);
                    return;
                }
                Map<Integer, List<Boolean>> taintTable = new HashMap<>();
                // Check the tainted status of return values when no parameter is tainted
                analyzeReturnTaintedStatus(invNode, null, symbolEnv);
                // If the table generation cannot be done because a depending function has not been analyzed
                if (this.blockedNode != null) {
                    // Set the function being analyzed to the map and clean the tree for next invocation.
                    this.blockedNode.invokableNode = invNode;
                    this.blockedNode = null;
                    return;
                }
                taintTable.put(-1, taintedStatus);
                if (taintedStatus.size() == 0) {
                    //TODO: Something is wrong if this get hit. Remove this before final commit.
                    this.dlog.error(invNode.pos, DiagnosticCode.RETURN_MUST_BE_TAINTED, "ERROR");
                    return;
                }
                // Compiler error if a return that is always tainted, has not been marked 'tainted'
                for (int i = 0; i < taintedStatus.size(); i++) {
                    BLangVariable retParam = invNode.retParams.get(i);
                    if (taintedStatus.get(i) && !retParam.flagSet.contains(Flag.TAINTED)) {
                        this.dlog.error(invNode.pos, DiagnosticCode.RETURN_MUST_BE_TAINTED, retParam.name.value);
                        return;
                        //TODO: Validate if return is the right thing to do
                    }
                }
                List<BLangVariable> params = invNode.params;
                for (int i = 0; i < params.size(); i++) {
                    BLangVariable param = params.get(i);
                    // If parameter is sensitive, it is invalid to have a case
                    // where tainted status of parameter is true.
                    if (param.flagSet.contains(Flag.SENSITIVE)) {
                        continue;
                    }
                    // Set new parameter state, analyze the body and see what is the outcome of the function.
                    analyzeReturnTaintedStatus(invNode, param, symbolEnv);
                    if (errorConditionObserved) {
                        errorConditionObserved = false;
                        return;
                    } else {
                        taintTable.put(i, taintedStatus);
                    }
                }
                //Clean-up old state.
                invNode.params.forEach(param -> param.symbol.tainted = false);
                invNode.symbol.taintTable = taintTable;
            } else {
                invNode.symbol.taintTable = new HashMap<>();
            }
        }
    }
    private void analyzeReturnTaintedStatus(BLangInvokableNode invNode, BLangVariable testParam, SymbolEnv symbolEnv) {
        //SymbolEnv stmtEnv = new SymbolEnv(invNode, this.env.scope);
        //this.env.copyTo(stmtEnv);
        //Clean-up old state
        invNode.params.forEach(param -> param.symbol.tainted = false);
        // Mark given parameter as tainted
        if (testParam != null) {
            invNode.params.get(invNode.params.indexOf(testParam)).symbol.tainted = true;
        }
        // Iteration to see the tainted status of returns when no input parameter is tainted
        if (invNode.workers.isEmpty()) {
            analyzeNode1(invNode.body, symbolEnv);
        } else {
            invNode.workers.forEach(worker -> analyzeNode1(worker, symbolEnv));
        }
    }
    /*private void validateParameters(List<BLangVariable> params, DiagnosticPos diagnosticPos,
                                    DiagnosticCode diagnosticCode) {
        params.forEach(param -> {
            if (param.getAnnotationAttachments() != null) {
                param.getAnnotationAttachments().forEach(annotationAttachment -> {
                    if (annotationAttachment.getAnnotationName().getValue().equals(SECURE_ANNOTATION_NAME)) {
                        if (param.symbol.tainted) {
                            this.dlog.error(diagnosticPos, diagnosticCode, param.name.value);
                        }
                    }
                });
            }
        });
    }*/
    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, env);
        structNode.fields.forEach(field -> analyzeNode1(field, structEnv));
    }

    @Override
    public void visit(BLangEnum enumNode) {
        enumNode.symbol.tainted = false;
    }

    public void visit(BLangAnnotation annotationNode) {

    }

    public void visit(BLangAnnotAttribute annotationAttribute) {

    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {

    }

    public void visit(BLangVariable varNode) {
        int ownerSymTag = env.scope.owner.tag;
        // Analyze the init expression
        if (varNode.expr != null) {
            // Here we create a new symbol environment to catch self references by keep the current
            // variable symbol in the symbol environment
            // e.g. int a = x + a;
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);

            // If the variable is a package/service/connector level variable, we don't need to check types.
            // It will we done during the init-function of the respective construct is visited.
            if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE &&
                    (ownerSymTag & SymTag.SERVICE) != SymTag.SERVICE &&
                    (ownerSymTag & SymTag.CONNECTOR) != SymTag.CONNECTOR) {
                analyzeNode1(varNode.expr, varInitEnv);
                if (varNode.expr instanceof BLangLambdaFunction) {
                    BLangFunction function = ((BLangLambdaFunction) varNode.expr).function;
                    //lambdaFunctions.put(new FunctionIdentifier(varNode.symbol.pkgID, varNode.getName()), function);
                } else {
                    setTaintedStatus(varNode, taintedStatus.get(0));
                }
            }
        }
    }

    // Statements

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        for (BLangStatement stmt : blockNode.stmts) {
            if (stopAnalysis) {
                stopAnalysis = false;
                break;
            } else {
                analyzeNode1(stmt, blockEnv);
            }
        }
    }

    public void visit(BLangVariableDef varDefNode) {
        varDefNode.var.accept(this);
    }

    public void visit(BLangAssignment assignNode) {
        assignNode.expr.accept(this);
        //boolean multiReturnHandledProperly = assignNode.varRefs != null && taintedStatus.size() ==
        //        assignNode.varRefs.size();
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression expr = assignNode.varRefs.get(i);
            if (expr instanceof BLangVariableReference) {
                //if (multiReturnHandledProperly) {
                    setTaintedStatus((BLangVariableReference) expr, taintedStatus.get(i));
               // } else {
               //     setTaintedStatus((BLangVariableReference) expr, taintedStatus.get(0));
               // }
            }
        }
    }

    public void visit(BLangBind bindNode) {
        bindNode.expr.accept(this);
        if (bindNode.varRef instanceof BLangVariableReference) {
            setTaintedStatus((BLangVariableReference) bindNode.varRef, taintedStatus.get(0));
        }
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        analyzeNode1(exprStmtNode.expr, stmtEnv);
    }

    public void visit(BLangIf ifNode) {
        ifNode.body.accept(this);
        if (ifNode.elseStmt != null) {
            //TODO: NOver
            ifNode.elseStmt.accept(this);
        }
    }

    public void visit(BLangForeach foreach) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        analyzeNode1(foreach.body, blockEnv);
    }

    public void visit(BLangWhile whileNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(whileNode.body, env);
        analyzeNode1(whileNode.body, blockEnv);
    }

    public void visit(BLangLock lockNode) {
        lockNode.body.accept(this);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        connectorNode.varDefs.forEach(varDef -> analyzeNode1(varDef, connectorEnv));
        analyzeNode1(connectorNode.initFunction, connectorEnv);
        analyzeNode1(connectorNode.initAction, connectorEnv);
        connectorNode.actions.forEach(action -> analyzeNode1(action, connectorEnv));
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;
        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        visitInvocable(actionNode, actionEnv);
    }

    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, env);
        serviceNode.vars.forEach(var -> analyzeNode1(var, serviceEnv));
        analyzeNode1(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(resource -> analyzeNode1(resource, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        visitInvocable(resourceNode, resourceEnv);
    }

    public void visit(BLangTryCatchFinally tryCatchFinally) {
        tryCatchFinally.tryBody.accept(this);
        //TODO: NOver
        tryCatchFinally.catchBlocks.forEach(c -> c.accept(this));
        if (tryCatchFinally.finallyBody != null) {
            tryCatchFinally.finallyBody.accept(this);
        }
    }

    public void visit(BLangCatch bLangCatch) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, env);
        analyzeNode1(bLangCatch.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody.accept(this);
        //TODO: NOver
        if (transactionNode.failedBody != null) {
            transactionNode.failedBody.accept(this);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // Do nothing
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        //TODO
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        analyzeNode1(workerNode.body, workerEnv);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.exprs.forEach(expr -> expr.accept(this));
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.exprs.forEach(expr -> expr.accept(this));
    }

    @Override
    public void visit(BLangReturn returnNode) {
        List<Boolean> taintedCheckResults = new ArrayList<>();
        if (returnNode.namedReturnVariables == null) {
            for (BLangExpression expr : returnNode.exprs) {
                expr.accept(this);
                taintedCheckResults.add(taintedStatus.get(0));
            }
        } else {
            for (BLangVariable var : returnNode.namedReturnVariables) {
                taintedCheckResults.add(var.symbol.tainted);
            }
        }
        taintedStatus = taintedCheckResults;
    }


    public void visit(BLangNext nextNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        //TODO
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, env);
        visitInvocable(transformerNode, transformerEnv);
    }

    // Expressions
    public void visit(BLangLiteral literalExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs.forEach(expr -> {
            expr.accept(this);
            if (taintedStatus.get(0)) {
                return;
            }
        });
        setTaintedStatusList(false);
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        boolean isTainted = false;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            keyValuePair.valueExpr.accept(this);

            // Make sure the symbol for key value pair is updated with the final decision if the pair is in symbol table
            if (keyValuePair.key.fieldSymbol != null) {
                keyValuePair.key.fieldSymbol.tainted = taintedStatus.get(0);
            }
            // To update the tainted status of the variable
            if (taintedStatus.get(0)) {
                isTainted = true;
            }
        }
        setTaintedStatusList(isTainted);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.symbol == null) {
            Name varName = names.fromIdNode(varRefExpr.variableName);
            if (varName != Names.IGNORE) {
                BSymbol symbol = symResolver.lookupSymbolInPackage(varRefExpr.pos, env,
                        names.fromIdNode(varRefExpr.pkgAlias), varName, SymTag.VARIABLE_NAME);
                setTaintedStatusList(symbol.tainted);
            }
        } else {
            setTaintedStatusList(varRefExpr.symbol.tainted);
        }
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BType varRefType = fieldAccessExpr.expr.type;
        Boolean isTainted = true;
        Name fieldName = names.fromIdNode(fieldAccessExpr.field);
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                BSymbol fieldSymbol = symResolver.resolveStructField(fieldAccessExpr.pos, this.env,
                        fieldName, varRefType.tsymbol);
                isTainted = fieldSymbol.tainted;
                break;
            case TypeTags.MAP:
                isTainted = fieldAccessExpr.expr.symbol.tainted;
                break;
            case TypeTags.JSON:
                BType constraintType = ((BJSONType) varRefType).constraint;
                if (constraintType.tag == TypeTags.STRUCT) {
                    //TODO Test this condition
                    BSymbol jsonFieldSymbol = symResolver.resolveStructField(fieldAccessExpr.pos, this.env,
                            fieldName, varRefType.tsymbol);
                    isTainted = jsonFieldSymbol.tainted;
                } else {
                    isTainted = fieldAccessExpr.expr.symbol.tainted;
                }
                break;
            case TypeTags.ENUM:
                isTainted = false;
                break;
        }
        setTaintedStatusList(isTainted);
    }

    public void visit(BLangIndexBasedAccess indexBasedAccessExpr) {
        indexBasedAccessExpr.expr.accept(this);
    }

    public void visit(BLangInvocation iExpr) {
        BLangInvokableNode invokableNode = null;
        if (iExpr.functionPointerInvocation) {
            if (iExpr.symbol instanceof BVarSymbol) {
                invokableNode = ((BVarSymbol) iExpr.symbol).node;
            } else {
                // TODO
                iExpr.symbol = iExpr.symbol;
            }
        } else if (iExpr.iterableOperationInvocation) {
            //visitIterableOperationInvocation(iExpr);
            return;
        } else {
            invokableNode = (BLangInvokableNode) ((BInvokableSymbol) iExpr.symbol).node;
        }
        if (invokableNode != null && invokableNode.params.size() > 0 && invokableNode.retParams.size() > 0) {
            if (invokableNode.symbol.taintTable == null) {
                if (!initialGenerationComplete) {
                    BlockingNode blockingNode = new BlockingNode(iExpr.symbol.pkgID, iExpr.symbol.name);
                    this.blockedNode = new BlockedNode(null, iExpr.pos);
                    List<BlockedNode> nodeList = blockedList.get(blockingNode);
                    if (nodeList == null) {
                        nodeList = new ArrayList<>();
                    }
                    nodeList.add(blockedNode);
                    blockedList.put(blockingNode, nodeList);
                    stopAnalysis = true;
                } else {
                    // This is an indication for invokable to skip any further analysis.
                    // TODO: Improve not to need a dummy object.
                    this.blockedNode = new BlockedNode(null, null);
                }
                //Return a dummy list to make sure following statements do not break.
                List<Boolean> returnTaintedStatus = new ArrayList<>();
                invokableNode.retParams.forEach(param -> returnTaintedStatus.add(false));
                taintedStatus = returnTaintedStatus;
            } else {
                Map<Integer, List<Boolean>> taintTable = invokableNode.symbol.taintTable;
                List<Boolean> returnTaintedStatus = taintTable.get(-1);
                for (int i = 0; i < iExpr.argExprs.size(); i++) {
                    iExpr.argExprs.get(i).accept(this);
                    // If current argument is tainted, look-up the taint-table for the record of
                    // return-tainted-status when the given argument is in tainted state.
                    if (taintedStatus.get(0)) {
                        List<Boolean> taintedStatusRecord = taintTable.get(i);
                        // This is null when current parameter is sensitive. Therefore, providing a tainted
                        // value to a sensitive parameter is invalid and should return a compiler error.
                        if (taintedStatusRecord == null) {
                            errorConditionObserved = true;
                            taintedStatus = taintTable.get(-1);
                        } else {
                            // Go through tainted status of each return. if return is tainted, update the
                            // returnTaintedStatus, so that it will have the accumulated tainted status of
                            // all returns.
                            for (int j = 0; j < taintedStatusRecord.size(); j++) {
                                boolean taintedStatus = taintedStatusRecord.get(j);
                                if (taintedStatus) {
                                    returnTaintedStatus.set(j, taintedStatus);
                                }
                            }
                        }
                    }
                }
                taintedStatus = returnTaintedStatus;
            }
        } else if (invokableNode.retParams.size() > 0) {
            List<Boolean> returnTaintedStatus = new ArrayList<>();
            invokableNode.retParams.forEach(retParam -> returnTaintedStatus
                    .add(retParam.flagSet.contains(Flag.TAINTED)));
            taintedStatus = returnTaintedStatus;
        }
    }

    public void visit(BLangConnectorInit cIExpr) {
        cIExpr = cIExpr;
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.thenExpr.accept(this);
        boolean thenTaintedCheckResult = taintedStatus.get(0);
        ternaryExpr.elseExpr.accept(this);
        boolean elseTaintedCheckResult = taintedStatus.get(0);
        setTaintedStatusList(thenTaintedCheckResult || elseTaintedCheckResult);
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr.accept(this);
        boolean lhsTaintedCheckResult = taintedStatus.get(0);
        binaryExpr.rhsExpr.accept(this);
        boolean rhsTaintedCheckResult = taintedStatus.get(0);
        setTaintedStatusList(lhsTaintedCheckResult || rhsTaintedCheckResult);
    }

    public void visit(BLangTypeofExpr accessExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
            setTaintedStatusList(false);
        } else {
            unaryExpr.expr.accept(this);
        }
    }

    public void visit(BLangTypeCastExpr castExpr) {
        castExpr.expr.accept(this);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr.accept(this);
    }


    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        //bLangLambdaFunction.function.accept(this);
        //lambdaFunctions.add(bLangLambdaFunction);
//        if (invocation != null) {
//            List<Boolean> taintedCheckResults = new ArrayList<>();
//            if (bLangLambdaFunction.function.retParams != null) {
//                for (BLangVariable returnVar : bLangLambdaFunction.function.retParams) {
//                    taintedCheckResults.add(returnVar.symbol.tainted);
//                }
//            }
//            taintedStatus = taintedCheckResults;
//        }
    }

    public void visit(BLangXMLQName bLangXMLQName) {
        setTaintedStatusList(false);
    }

    public void visit(BLangXMLAttribute bLangXMLAttribute) {
        //TODO check if symbol should be set here
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(bLangXMLAttribute, env);

        // check attribute name
        bLangXMLAttribute.name.accept(this);
        boolean attrNameTainedStatus = taintedStatus.get(0);

        // check attribute value
        bLangXMLAttribute.value.accept(this);
        boolean attrValueTainedStatus = taintedStatus.get(0);

        setTaintedStatusList(attrNameTainedStatus || attrValueTainedStatus);
    }

    public void visit(BLangXMLElementLiteral bLangXMLElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(bLangXMLElementLiteral, env);

        // Visit in-line namespace declarations
        boolean inLineNamespaceTainted = false;
        for (BLangXMLAttribute attribute : bLangXMLElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && ((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                BLangExpression bLangExpression = attribute;
                bLangExpression.accept(this);
                attribute.symbol.tainted = taintedStatus.get(0);
                if (attribute.symbol.tainted) {
                    inLineNamespaceTainted = true;
                }
            }
        }

        // Visit attributes.
        boolean attributesTainted = false;
        for (BLangXMLAttribute attribute : bLangXMLElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && !((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                BLangExpression bLangExpression = attribute;
                bLangExpression.accept(this);
                attribute.symbol.tainted = taintedStatus.get(0);
                if (attribute.symbol.tainted) {
                    attributesTainted = true;
                }
            }
        }

        Map<Name, BXMLNSSymbol> namespaces = symResolver.resolveAllNamespaces(xmlElementEnv);
        Name defaultNs = names.fromString(XMLConstants.DEFAULT_NS_PREFIX);
        if (namespaces.containsKey(defaultNs)) {
            bLangXMLElementLiteral.defaultNsSymbol = namespaces.remove(defaultNs);
        }
        bLangXMLElementLiteral.namespacesInScope.putAll(namespaces);

        // Visit the tag names
        BLangExpression startTagName = bLangXMLElementLiteral.startTagName;
        startTagName.accept(this);
        boolean startTagTaintedCheckResult = taintedStatus.get(0);
        BLangExpression endTagName = bLangXMLElementLiteral.endTagName;
        boolean endTagTaintedCheckResult = false;
        if (endTagName != null) {
            endTagName.accept(this);
            endTagTaintedCheckResult = taintedStatus.get(0);
        }
        boolean tagNamesTainted = startTagTaintedCheckResult || endTagTaintedCheckResult;

        // Visit the children
        boolean childrenTainted = false;
        for (BLangExpression expr : bLangXMLElementLiteral.children) {
            expr.accept(this);
            boolean childTaintedCheckResult = taintedStatus.get(0);
            if (childTaintedCheckResult) {
                childrenTainted = true;
            }
        }

        setTaintedStatusList(inLineNamespaceTainted || attributesTainted || tagNamesTainted || childrenTainted);
    }

    public void visit(BLangXMLTextLiteral bLangXMLTextLiteral) {
        analyzeStringTemplateConcatExpr(bLangXMLTextLiteral.textFragments);
    }

    public void visit(BLangXMLCommentLiteral bLangXMLCommentLiteral) {
        analyzeStringTemplateConcatExpr(bLangXMLCommentLiteral.textFragments);
    }

    public void visit(BLangXMLProcInsLiteral bLangXMLProcInsLiteral) {
        bLangXMLProcInsLiteral.target.accept(this);
        if (!taintedStatus.get(0)) {
            analyzeStringTemplateConcatExpr(bLangXMLProcInsLiteral.dataFragments);
        }
    }

    public void visit(BLangXMLQuotedString bLangXMLQuotedString) {
        analyzeStringTemplateConcatExpr(bLangXMLQuotedString.textFragments);
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        //TODO
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        analyzeStringTemplateConcatExpr(stringTemplateLiteral.exprs);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        setTaintedStatusList(false);
    }

    // Private methods
    private <T extends BLangExpression, U extends SymbolEnv> void analyzeExpr(T t, U u) {
       analyzeNode1(t, u);
    }

    private <T extends BLangNode, U extends SymbolEnv> void analyzeNode1(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
    }

    private void analyzeStringTemplateConcatExpr(List<BLangExpression> exprs) {
        for (BLangExpression expr : exprs) {
            expr.accept(this);
            if (taintedStatus.get(0)) {
                break;
            }
        }
    }

    private void setTaintedStatus(BLangVariable varNode, boolean taintedCheckResult) {
        // In an non overriding context (such as else condition of a if statement,
        // it should not be allowed to change the tainted status from true to false
        // this prevents identifying variable "example" as untainted.
        // var example = "test"; if (condition) { example = "untainted"; } else { example = taintedValue; }
        if (!nonOverridingContext) {
            varNode.symbol.tainted = taintedCheckResult;
        } else if (nonOverridingContext && !varNode.symbol.tainted) {
            varNode.symbol.tainted = taintedCheckResult;
        }
    }

    private void setTaintedStatus(BLangVariableReference varNode, boolean taintedCheckResult) {
        // In an non overriding context (such as else condition of a if statement,
        // it should not be allowed to change the tainted status from true to false
        // this prevents identifying variable "example" as untainted.
        // var example = "test"; if (condition) { example = "untainted"; } else { example = taintedValue; }
        if (!nonOverridingContext) {
            varNode.symbol.tainted = taintedCheckResult;
        } else if (nonOverridingContext && !varNode.symbol.tainted) {
            varNode.symbol.tainted = taintedCheckResult;
        }
    }

    private void setTaintedStatusList(boolean taintedStatus) {
        List<Boolean> taintedStatusList = new ArrayList<>();
        taintedStatusList.add(taintedStatus);
        this.taintedStatus = taintedStatusList;
    }

    private class FunctionIdentifier {
        public PackageID packageId;
        public BLangIdentifier identifier;

        public FunctionIdentifier(PackageID packageId, BLangIdentifier identifier) {
            this.packageId = packageId;
            this.identifier = identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FunctionIdentifier that = (FunctionIdentifier) o;
            return packageId.equals(that.packageId) &&
                    identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            int result = packageId.hashCode();
            result = 31 * result + identifier.hashCode();
            return result;
        }
    }

    private void attachTaintTableBasedOnFlags(BLangInvokableNode invNode) {
        if (invNode.symbol.taintTable == null) {
            Map<Integer, List<Boolean>> taintTable = new HashMap<>();
            // Extract tainted status of native function from the return modifier.
            List<Boolean> retParamsTaintedStatus = new ArrayList<>();
            for (BLangVariable retParam : invNode.retParams) {
                retParamsTaintedStatus.add(retParam.flagSet.contains(Flag.TAINTED));
            }

            // Add tainted status when no parameter is tainted.
            List<BLangVariable> params = invNode.params;
            taintTable.put(-1, retParamsTaintedStatus);

            // Add taubred status when each parameter is tainted.
            for (int i = 0; i < params.size(); i++) {
                // If parameter is sensitive, it is invalid to have a case
                // where tainted status of parameter is true.
                if (params.get(i).flagSet.contains(Flag.SENSITIVE)) {
                    continue;
                }
                taintTable.put(i, retParamsTaintedStatus);
            }
            invNode.symbol.taintTable = taintTable;
        }
    }

    private class BlockingNode {
        public PackageID packageID;
        public Name name;

        public BlockingNode(PackageID packageID, Name name) {
            this.packageID = packageID;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BlockingNode that = (BlockingNode) o;

            if (!packageID.equals(that.packageID)) {
                return false;
            }
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = packageID.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    private class BlockedNode {
        public BLangInvokableNode invokableNode;
        public DiagnosticPos blockedPos;

        public BlockedNode(BLangInvokableNode invokableNode, DiagnosticPos blockedPos) {
            this.invokableNode = invokableNode;
            this.blockedPos = blockedPos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BlockedNode that = (BlockedNode) o;

            if (!invokableNode.equals(that.invokableNode)) {
                return false;
            }
            return blockedPos.equals(that.blockedPos);
        }

        @Override
        public int hashCode() {
            int result = invokableNode.hashCode();
            result = 31 * result + blockedPos.hashCode();
            return result;
        }
    }
}
