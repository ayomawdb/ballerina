package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;

/**
 * Perform taint analysis.
 */
public class TaintAnalyzer  extends BLangNodeVisitor {
    private static final CompilerContext.Key<TaintAnalyzer> TAINT_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolEnv env;
    private SymbolEnv mainPkgEnv;
    private SymbolEnv currPkgEnv;
    private Names names;
    private SymbolEnter symbolEnter;
    private DiagnosticLog dlog;

    private List<Boolean> taintedStatusList;
    private List<Boolean> returnTaintedStatusList;
    private boolean nonOverridingContext;
    private boolean initialGenerationComplete;
    private boolean entryPointAnalysis;
    private boolean stopAnalysis;

    private TaintError taintError;
    private BlockedNode blockedNode;
    private Set<BlockedNode> blockedNodes = new HashSet<>();

    private static final String MAIN_FUNCTION_NAME = "main";
    private static final int ALL_UNTAINTED_TABLE_ENTRY_INDEX = -1;

    private static final String ANNOTATION_TAINTED = "tainted";
    private static final String ANNOTATION_UNTAINTED = "untainted";
    private static final String ANNOTATION_SENSITIVE = "sensitive";

    public static TaintAnalyzer getInstance(CompilerContext context) {
        TaintAnalyzer taintAnalyzer = context.get(TAINT_ANALYZER_KEY);
        if (taintAnalyzer == null) {
            taintAnalyzer = new TaintAnalyzer(context);
        }
        return taintAnalyzer;
    }

    public TaintAnalyzer(CompilerContext context) {
        context.put(TAINT_ANALYZER_KEY, this);
        this.names = Names.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        this.mainPkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        pkgNode.accept(this);
        return pkgNode;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TAINT_ANALYZE)) {
            return;
        }
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);
        SymbolEnv prevPkgEnv = this.currPkgEnv;
        this.currPkgEnv = pkgEnv;
        this.env = pkgEnv;

        pkgNode.imports.forEach(impPkgNode -> impPkgNode.accept(this));
        pkgNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
        // Do table generation for blocked invokable, only after all the import packages are scanned.
        if (this.mainPkgEnv.equals(pkgEnv)) {
            initialGenerationComplete = true;
            resolveBlockedInvokables();
            initialGenerationComplete = false;
        }
        this.currPkgEnv = prevPkgEnv;
        pkgNode.completedPhases.add(CompilerPhase.TAINT_ANALYZE);
    }

    public void visit(BLangCompilationUnit compUnit) {
        compUnit.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        /* ignore */
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

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        if (isEntryPoint(funcNode)) {
            visitEntrypoint(funcNode, funcEnv);
        } else {
            visitInvokable(funcNode, funcEnv);
        }
    }

    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, env);
        serviceNode.vars.forEach(var -> analyzeNode(var, serviceEnv));
        analyzeNode(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(resource -> analyzeNode(resource, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        visitEntrypoint(resourceNode, resourceEnv);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        analyzeNode(connectorNode.initFunction, connectorEnv);
        analyzeNode(connectorNode.initAction, connectorEnv);
        connectorNode.actions.forEach(action -> analyzeNode(action, connectorEnv));
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;
        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        visitInvokable(actionNode, actionEnv);
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, env);
        structNode.fields.forEach(field -> analyzeNode(field, structEnv));
    }

    public void visit(BLangEnum enumNode) {
        enumNode.symbol.tainted = false;
    }

    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        /* ignore */
    }

    public void visit(BLangVariable varNode) {
        int ownerSymTag = env.scope.owner.tag;
        if (varNode.expr != null) {
             SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);
            // If the variable is a package/service/connector level variable, we don't need to check types.
            // It will we done during the init-function of the respective construct is visited.
            if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE &&
                    (ownerSymTag & SymTag.SERVICE) != SymTag.SERVICE &&
                    (ownerSymTag & SymTag.CONNECTOR) != SymTag.CONNECTOR) {
                if (varNode.expr instanceof BLangConnectorInit) {
                    analyzeNode(varNode.expr, varInitEnv);
                } else {
                    analyzeNode(varNode.expr, varInitEnv);
                }
                if (taintedStatusList == null || taintedStatusList.size() == 0) {
                    varNode.expr = varNode.expr;
                }
                setTaintedStatus(varNode, taintedStatusList.get(0));
            }
        }
    }

    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        analyzeNode(workerNode.body, workerEnv);
    }

    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    public void visit(BLangAnnotation annotationNode) {
        annotationNode = annotationNode;
        /* ignore */
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        /* ignore */
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        /* ignore */
    }

    public void visit(BLangTransformer transformerNode) {
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, env);
        visitInvokable(transformerNode, transformerEnv);
    }

    // Statements
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        for (BLangStatement stmt : blockNode.stmts) {
            if (stopAnalysis) {
                break;
            } else {
                analyzeNode(stmt, blockEnv);
            }
        }
        if (stopAnalysis) {
            stopAnalysis = false;
        }
    }

    public void visit(BLangVariableDef varDefNode) {
        varDefNode.var.accept(this);
    }

    public void visit(BLangAssignment assignNode) {
        assignNode.expr.accept(this);
        // True when second return of a multi return skipped.
        boolean multiReturnHandledProperly = assignNode.varRefs != null && taintedStatusList.size() ==
                assignNode.varRefs.size();
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression expr = assignNode.varRefs.get(i);
            boolean taintedCheckResult = taintedStatusList.get(0);
            if (multiReturnHandledProperly) {
                taintedCheckResult = taintedStatusList.get(i);
            }
            // TODO: Re-evaluating the full data-set (array) when a change occur.
            if (expr instanceof BLangIndexBasedAccess) {
                nonOverridingContext = true;
                updatedVarRefTaintedState((BLangIndexBasedAccess) expr, taintedCheckResult);
                nonOverridingContext = false;
            } else if (expr instanceof BLangFieldBasedAccess) {
                if (((BLangFieldBasedAccess) expr).symbol != null) {
                    setTaintedStatus((BLangVariableReference) expr, taintedCheckResult);
                }
                nonOverridingContext = true;
                updatedVarRefTaintedState((BLangFieldBasedAccess) expr, taintedCheckResult);
                nonOverridingContext = false;
            } else {
                setTaintedStatus((BLangVariableReference) expr, taintedCheckResult);
            }
        }
    }

    private void updatedVarRefTaintedState(BLangVariableReference varRef, boolean taintedState) {
        if (varRef instanceof BLangSimpleVarRef) {
            setTaintedStatus(varRef, taintedState);
        } else if (varRef instanceof BLangIndexBasedAccess) {
            updatedVarRefTaintedState((((BLangIndexBasedAccess) varRef).expr), taintedState);
        } else if (varRef instanceof BLangFieldBasedAccess) {
            updatedVarRefTaintedState((((BLangFieldBasedAccess) varRef).expr), taintedState);
        }
    }

    public void visit(BLangBind bindNode) {
        bindNode.expr.accept(this);
        if (bindNode.varRef instanceof BLangVariableReference) {
            setTaintedStatus((BLangVariableReference) bindNode.varRef, taintedStatusList.get(0));
        }
    }

    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    public void visit(BLangNext nextNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    public void visit(BLangReturn returnNode) {
        List<Boolean> taintedCheckResults = new ArrayList<>();
        if (returnNode.namedReturnVariables == null) {
            for (BLangExpression expr : returnNode.exprs) {
                expr.accept(this);
                taintedCheckResults.addAll(taintedStatusList);
            }
        } else {
            for (BLangVariable var : returnNode.namedReturnVariables) {
                taintedCheckResults.add(var.symbol.tainted);
            }
        }
        taintedStatusList = taintedCheckResults;
        if (returnTaintedStatusList == null) {
            returnTaintedStatusList = taintedCheckResults;
        } else {
            for (int i = 0; i < returnTaintedStatusList.size(); i++) {
                if (!returnTaintedStatusList.get(i)) {
                    returnTaintedStatusList.set(i, taintedCheckResults.get(i));
                }
            }
        }
    }

    public void visit(BLangReturn.BLangWorkerReturn returnNode) {
        /* ignore */
    }

    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        analyzeNode(exprStmtNode.expr, stmtEnv);
    }

    public void visit(BLangIf ifNode) {
        ifNode.body.accept(this);
        nonOverridingContext = true;
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
        nonOverridingContext = false;
    }

    public void visit(BLangForeach foreach) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        analyzeNode(foreach.body, blockEnv);
    }

    public void visit(BLangWhile whileNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(whileNode.body, env);
        analyzeNode(whileNode.body, blockEnv);
    }

    public void visit(BLangLock lockNode) {
        lockNode.body.accept(this);
    }

    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody.accept(this);
        nonOverridingContext = true;
        if (transactionNode.failedBody != null) {
            transactionNode.failedBody.accept(this);
        }
        nonOverridingContext = false;
    }

    public void visit(BLangTryCatchFinally tryNode) {
        tryNode.tryBody.accept(this);
        nonOverridingContext = true;
        tryNode.catchBlocks.forEach(c -> c.accept(this));
        if (tryNode.finallyBody != null) {
            tryNode.finallyBody.accept(this);
        }
        nonOverridingContext = false;
    }

    public void visit(BLangCatch catchNode) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(catchNode.body, env);
        analyzeNode(catchNode.body, catchBlockEnv);
    }

    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(worker -> worker.accept(this));
        nonOverridingContext = true;
        if (forkJoin.joinedBody != null) {
            forkJoin.joinedBody.accept(this);
        }
        if (forkJoin.timeoutBody != null) {
            forkJoin.timeoutBody.accept(this);
        }
        nonOverridingContext = false;
    }

    // Expressions

    public void visit(BLangLiteral literalExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        if (arrayLiteral.exprs.size() == 0) {
            // Empty arrays are untainted.
            setTaintedStatusList(false);
        } else {
            for (BLangExpression expr : arrayLiteral.exprs) {
                expr.accept(this);
                if (taintedStatusList.get(0)) {
                    break;
                }
            }
        }
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        boolean isTainted = false;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            keyValuePair.valueExpr.accept(this);
            // Update struct field symbol with individual field tainted status.
            if (keyValuePair.key.fieldSymbol != null) {
                keyValuePair.key.fieldSymbol.tainted = taintedStatusList.get(0);
            }
            // Used to update the variable this literal is getting assigned to.
            if (taintedStatusList.get(0)) {
                isTainted = true;
            }
        }
        setTaintedStatusList(isTainted);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.symbol == null) {
            Name varName = names.fromIdNode(varRefExpr.variableName);
            if (varName != Names.IGNORE) {
                if (varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
                    setTaintedStatusList(varRefExpr.pkgSymbol.tainted);
                } else {
                    setTaintedStatusList(false);
                }
            }
        } else {
            setTaintedStatusList(varRefExpr.symbol.tainted);
        }
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BType varRefType = fieldAccessExpr.expr.type;
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                fieldAccessExpr.expr.accept(this);
                //TODO: Improve to use field level tainted status.
                break;
            case TypeTags.MAP:
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.JSON:
                fieldAccessExpr.expr.accept(this);
                //TODO: Improve to use field level tainted status.
                break;
            case TypeTags.ENUM:
                setTaintedStatusList(false);
                break;
        }
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.expr.accept(this);
    }

    public void visit(BLangInvocation invocationExpr) {
        BLangInvokableNode invokableNode = null;
        if (invocationExpr.functionPointerInvocation) {
            invokableNode = ((BVarSymbol) invocationExpr.symbol).node;
            if (invokableNode == null) {
                // Skip function pointers and assume returns of function pointer executions are untainted.
                // TODO: Check the ability of resolving function pointers and perform analysis.
                List<Boolean> returnTaintedStatus = new ArrayList<>();
                ((BInvokableType) invocationExpr.symbol.type).retTypes
                        .forEach(retType -> returnTaintedStatus.add(false));
                taintedStatusList = returnTaintedStatus;
                return;
            }
        } else if (invocationExpr.iterableOperationInvocation) {
            invocationExpr.expr.accept(this);
            if (taintedStatusList.get(0)) {
                if (invocationExpr.argExprs != null) {
                    invocationExpr.argExprs.forEach(expr -> {
                        if (expr instanceof BLangLambdaFunction) {
                            BLangFunction function = ((BLangLambdaFunction) expr).function;
                            if (function.symbol.taintTable == null) {
                                addToBlockedList(invocationExpr, 1);
                            } else {
                                Map<Integer, List<Boolean>> taintTable = function.symbol.taintTable;
                                // If expr is tainted, all arguments are considered tainted. Hence, if any argument
                                // is tainted, generate error.
                                for (int i = 0; i < function.params.size(); i++) {
                                    List<Boolean> taintedStatusRecord = taintTable.get(i);
                                    if (taintedStatusRecord == null) {
                                        if (entryPointAnalysis) {
                                            this.dlog.error(invocationExpr.pos,
                                                    DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER,
                                                    function.params.get(i).name.value);
                                        } else {
                                            taintError = new TaintError(invocationExpr.pos,
                                                    function.params.get(i).name.value);
                                            stopAnalysis = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
            return;
        } else {
            invokableNode = (BLangInvokableNode) ((BInvokableSymbol) invocationExpr.symbol).node;
        }
        if (invokableNode.symbol.taintTable == null) {
            addToBlockedList(invocationExpr, invokableNode.retParams.size());
        } else {
            setTaintedStatus(invocationExpr, invokableNode);
        }
    }

    private void addToBlockedList(BLangInvocation invocationExpr, int returnCount) {
        BlockingNode blockingNode = new BlockingNode(invocationExpr.symbol.pkgID, invocationExpr.symbol.name);
        this.blockedNode = new BlockedNode(this.currPkgEnv, null, invocationExpr.pos, blockingNode);
        stopAnalysis = true;
        List<Boolean> returnTaintedStatus = new ArrayList<>();
        for (int i = 0; i < returnCount; i++) {
            returnTaintedStatus.add(false);
        }
        taintedStatusList = returnTaintedStatus;
    }

    private void setTaintedStatus(BLangInvocation invocationExpr, BLangInvokableNode invokableNode) {
        Map<Integer, List<Boolean>> taintTable = invokableNode.symbol.taintTable;
        List<Boolean> returnTaintedStatus = taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);

            if (invocationExpr.argExprs != null) {
                for (int i = 0; i < invocationExpr.argExprs.size(); i++) {
                    BLangExpression argExpr = invocationExpr.argExprs.get(i);
                    argExpr.accept(this);
                    // If current argument is tainted, look-up the taint-table for the record of
                    // return-tainted-status when the given argument is in tainted state.
                    if (taintedStatusList.get(0)) {
                        List<Boolean> taintedStatusRecord = taintTable.get(i);
                        // This is null when current parameter is sensitive. Therefore, providing a tainted
                        // value to a sensitive parameter is invalid and should return a compiler error.
                        if (taintedStatusRecord == null) {
                            if (entryPointAnalysis) {
                                this.dlog.error(invocationExpr.pos,
                                        DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER,
                                        invokableNode.params.get(i).name.value);
                            } else {
                                taintError = new TaintError(invocationExpr.pos, invokableNode.params.get(i).name.value);
                                stopAnalysis = true;
                                break;
                            }
                        } else {
                            // Go through tainted status of each return. if return is tainted, update the
                            // returnTaintedStatusList, so that it will have the accumulated tainted status of
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
            }
        if (invocationExpr.expr != null) {
            // When an invocation like stringValue.trim() happens, if stringValue is tainted, the result will
            // also be tainted, unless it was explicitly marked untainted using @untainted with returns.
            invocationExpr.expr.accept(this);
            if (taintedStatusList.get(0)) {
                returnTaintedStatus = new ArrayList<>();
                for (BLangVariable retParam : invokableNode.retParams) {
                    if (hasAnnotation(retParam, ANNOTATION_UNTAINTED)) {
                        returnTaintedStatus.add(false);
                    } else {
                        returnTaintedStatus.add(true);
                    }
                }
            }
        }
        taintedStatusList = returnTaintedStatus;
    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.thenExpr.accept(this);
        boolean thenTaintedCheckResult = taintedStatusList.get(0);

        nonOverridingContext = true;
        ternaryExpr.elseExpr.accept(this);
        nonOverridingContext = false;

        boolean elseTaintedCheckResult = taintedStatusList.get(0);
        setTaintedStatusList(thenTaintedCheckResult || elseTaintedCheckResult);
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr.accept(this);
        boolean lhsTaintedCheckResult = taintedStatusList.get(0);
        binaryExpr.rhsExpr.accept(this);
        boolean rhsTaintedCheckResult = taintedStatusList.get(0);
        setTaintedStatusList(lhsTaintedCheckResult || rhsTaintedCheckResult);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        switch (unaryExpr.operator) {
            case TYPEOF:
                setTaintedStatusList(false);
                break;
            case LENGTHOF:
                setTaintedStatusList(false);
                break;
            case UNTAINT:
                setTaintedStatusList(false);
                break;
            default:
                unaryExpr.expr.accept(this);
                break;
        }
    }

    public void visit(BLangTypeofExpr accessExpr) {
        setTaintedStatusList(false);
    }

    public void visit(BLangTypeCastExpr castExpr) {
        castExpr.expr.accept(this);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr.accept(this);
    }

    public void visit(BLangXMLQName xmlQName) {
        setTaintedStatusList(false);
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(xmlAttribute, env);
        xmlAttribute.name.accept(this);
        boolean attrNameTainedStatus = taintedStatusList.get(0);
        xmlAttribute.value.accept(this);
        boolean attrValueTainedStatus = taintedStatusList.get(0);
        setTaintedStatusList(attrNameTainedStatus || attrValueTainedStatus);
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(xmlElementLiteral, env);

        // Visit in-line namespace declarations
        boolean inLineNamespaceTainted = false;
        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && ((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                BLangExpression bLangExpression = attribute;
                bLangExpression.accept(this);
                attribute.symbol.tainted = taintedStatusList.get(0);
                if (attribute.symbol.tainted) {
                    inLineNamespaceTainted = true;
                }
            }
        }

        // Visit attributes.
        boolean attributesTainted = false;
        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && !((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                BLangExpression bLangExpression = attribute;
                bLangExpression.accept(this);
                attribute.symbol.tainted = taintedStatusList.get(0);
                if (attribute.symbol.tainted) {
                    attributesTainted = true;
                }
            }
        }

        // Visit the tag names
        BLangExpression startTagName = xmlElementLiteral.startTagName;
        startTagName.accept(this);
        boolean startTagTaintedCheckResult = taintedStatusList.get(0);
        BLangExpression endTagName = xmlElementLiteral.endTagName;
        boolean endTagTaintedCheckResult = false;
        if (endTagName != null) {
            endTagName.accept(this);
            endTagTaintedCheckResult = taintedStatusList.get(0);
        }
        boolean tagNamesTainted = startTagTaintedCheckResult || endTagTaintedCheckResult;

        // Visit the children
        boolean childrenTainted = false;
        for (BLangExpression expr : xmlElementLiteral.children) {
            expr.accept(this);
            boolean childTaintedCheckResult = taintedStatusList.get(0);
            if (childTaintedCheckResult) {
                childrenTainted = true;
            }
        }

        setTaintedStatusList(inLineNamespaceTainted || attributesTainted || tagNamesTainted || childrenTainted);
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        analyzeStringTemplateConcatExpr(xmlTextLiteral.textFragments);
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        analyzeStringTemplateConcatExpr(xmlCommentLiteral.textFragments);
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target.accept(this);
        if (!taintedStatusList.get(0)) {
            analyzeStringTemplateConcatExpr(xmlProcInsLiteral.dataFragments);
        }
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        analyzeStringTemplateConcatExpr(xmlQuotedString.textFragments);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        analyzeStringTemplateConcatExpr(stringTemplateLiteral.exprs);
    }

    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.exprs.forEach(expr -> expr.accept(this));
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        //TODO: Be blocked on other senders and check tainted status of data based on outputs of other workers.
        workerReceiveNode.exprs.forEach(expr -> expr.accept(this));
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        /* ignore */
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.expr.accept(this);
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        setTaintedStatusList(false);
    }

    // Private

    private <T extends BLangExpression, U extends SymbolEnv> void analyzeExpr(T t, U u) {
        analyzeNode(t, u);
    }

    private <T extends BLangNode, U extends SymbolEnv> void analyzeNode(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
    }

    private void analyzeStringTemplateConcatExpr(List<BLangExpression> exprs) {
        for (BLangExpression expr : exprs) {
            expr.accept(this);
            if (taintedStatusList.get(0)) {
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
        this.taintedStatusList = taintedStatusList;
    }

    private void attachTaintTableBasedOnFlags(BLangInvokableNode invNode) {
        if (invNode.symbol.taintTable == null) {
            Map<Integer, List<Boolean>> taintTable = new HashMap<>();
            // Extract tainted status of native function from the return modifier.
            List<Boolean> retParamsTaintedStatus = new ArrayList<>();
            for (BLangVariable retParam : invNode.retParams) {
                retParamsTaintedStatus.add(hasAnnotation(retParam, ANNOTATION_TAINTED));
            }

            // Add tainted status when no parameter is tainted.
            List<BLangVariable> params = invNode.params;
            taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, retParamsTaintedStatus);

            if (invNode.params.size() > 0) {
                // Add taubred status when each parameter is tainted.
                for (int i = 0; i < params.size(); i++) {
                    // If parameter is sensitive, it is invalid to have a case
                    // where tainted status of parameter is true.
                    if (hasAnnotation(params.get(i), ANNOTATION_SENSITIVE)) {
                        continue;
                    }
                    taintTable.put(i, retParamsTaintedStatus);
                }
            }
            invNode.symbol.taintTable = taintTable;
        }
    }

    private void resolveBlockedInvokables() {
        while (blockedNodes.size() > 0) {
            Set<BlockedNode> remainingBlockedNodes = new HashSet<>();
            for (BlockedNode blockedNode : blockedNodes) {
                this.env = blockedNode.pkgSymbol;
                blockedNode.invokableNode.accept(this);
                if (blockedNode.invokableNode.symbol.taintTable == null) {
                    remainingBlockedNodes.add(blockedNode);
                }
            }
            if (blockedNodes.size() == remainingBlockedNodes.size()) {
                for (BlockedNode blockedNode : blockedNodes) {
                    attachTaintTableBasedOnFlags(blockedNode.invokableNode);
                    this.dlog.warning(blockedNode.blockedPos,
                            DiagnosticCode.UNABLE_TO_PERFORM_TAINT_CHECKING_WITH_INVOCATION_LOOP,
                            blockedNode.invokableNode.name.value);
                }
                break;
            }
            blockedNodes = remainingBlockedNodes;
        }
    }

    private boolean isEntryPoint(BLangFunction funcNode) {
        // Service resources are handled through BLangResource.
        boolean isMainFunction = false;
        if (funcNode.name.value.equals(MAIN_FUNCTION_NAME) && funcNode.symbol.params.size() == 1
                && funcNode.symbol.retParams.size() == 0) {
            BType paramType = funcNode.symbol.params.get(0).type;
            BArrayType arrayType = (BArrayType) paramType;
            if (paramType.tag == TypeTags.ARRAY && arrayType.eType.tag == TypeTags.STRING) {
                isMainFunction = true;
            }
        }
        return isMainFunction;
    }

    private void visitEntrypoint(BLangInvokableNode invNode, SymbolEnv funcEnv) {
        entryPointAnalysis = true;
        if (invNode.params != null) {
            invNode.params.forEach(param -> param.symbol.tainted = true);
        }
        analyzeReturnTaintedStatus(invNode, funcEnv);
        if (this.blockedNode != null) {
            // Set the function being analyzed to the map and clean the tree for next invocation.
            this.blockedNode.invokableNode = invNode;
            if (!initialGenerationComplete) {
                blockedNodes.add(blockedNode);
            }
            this.blockedNode = null;
            return;
        }
        invNode.symbol.taintTable = new HashMap<>();
        invNode.params.forEach(param -> param.symbol.tainted = false);
        entryPointAnalysis = false;
    }

    private void visitInvokable(BLangInvokableNode invNode, SymbolEnv symbolEnv) {
        if (invNode.symbol.taintTable == null) {
            if (Symbols.isNative(invNode.symbol)) {
                attachTaintTableBasedOnFlags(invNode);
                return;
            }
            Map<Integer, List<Boolean>> taintTable = new HashMap<>();
            returnTaintedStatusList = null;
            // Check the tainted status of return values when no parameter is tainted.
            analyzeReturnTaintedStatus(taintTable, invNode, symbolEnv, ALL_UNTAINTED_TABLE_ENTRY_INDEX);
            // If the table generation cannot be done because a depending function has not been analyzed
            if (this.blockedNode != null) {
                // Set the function being analyzed to the map and clean the tree for next invocation.
                this.blockedNode.invokableNode = invNode;
                if (!initialGenerationComplete) {
                    blockedNodes.add(blockedNode);
                }
                this.blockedNode = null;
                return;
            }
            List<BLangVariable> params = invNode.params;
            for (int i = 0; i < params.size(); i++) {
                BLangVariable param = params.get(i);
                // If parameter is sensitive, it is invalid to have a case
                // where tainted status of parameter is true.
                if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                    continue;
                }
                returnTaintedStatusList = null;
                // Set new parameter state, analyze the body and see what is the outcome of the function.
                analyzeReturnTaintedStatus(taintTable, invNode, symbolEnv, i);
            }
            //Clean-up old state.
            invNode.params.forEach(param -> param.symbol.tainted = false);
            invNode.symbol.taintTable = taintTable;
        }
    }

    private void analyzeReturnTaintedStatus(Map<Integer, List<Boolean>> taintTable, BLangInvokableNode invNode,
                                            SymbolEnv symbolEnv, int paramIndex) {
        //Clean-up old state
        if (invNode.params != null) {
            invNode.params.forEach(param -> param.symbol.tainted = false);
        }
        // Mark given parameter as tainted
        if (paramIndex != -1) {
            invNode.params.get(paramIndex).symbol.tainted = true;
        }
        analyzeReturnTaintedStatus(invNode, symbolEnv);
        if (taintError != null) {
            // When invocation returns an error, due to passing a tainted argument to a sensitive parameter
            // skip adding the current invalid combination to the table. This will infer the sensitive
            // state of invocations, into owner.
            taintError = null;
        } else {
            if (invNode.retParams.size() == 0) {
                returnTaintedStatusList = new ArrayList<>();
            } else {
                updateBasedOnFlags(invNode.retParams);
            }
            taintTable.put(paramIndex, returnTaintedStatusList);
        }
    }

    private void updateBasedOnFlags(List<BLangVariable> retParams) {
        if (returnTaintedStatusList != null) {
            for (int i = 0; i < retParams.size(); i++) {
                BLangVariable param = retParams.get(i);
                boolean observedReturnTaintedStatus = returnTaintedStatusList.get(i);
                if (observedReturnTaintedStatus) {
                    // If return is tainted, but return is marked as untainted, overwrite the value.
                    if (hasAnnotation(param, ANNOTATION_UNTAINTED)) {
                        returnTaintedStatusList.set(i, false);
                    }
                } else {
                    // If return is not tainted, but return is marked as tainted, overwrite the value.
                    if (hasAnnotation(param, ANNOTATION_TAINTED)) {
                        returnTaintedStatusList.set(i, true);
                    }
                }
            }
        } else {
            returnTaintedStatusList = new ArrayList<>();
        }
    }

    private void analyzeReturnTaintedStatus(BLangInvokableNode invNode, SymbolEnv symbolEnv) {
        // Iteration to see the tainted status of returns when no input parameter is tainted
        if (invNode.workers.isEmpty()) {
            analyzeNode(invNode.body, symbolEnv);
        } else {
            for (BLangWorker worker : invNode.workers) {
                analyzeNode(worker, symbolEnv);
                if (this.blockedNode != null || taintError != null) {
                    break;
                }
            }
        }
    }

    private boolean hasAnnotation(BLangVariable variable, String expectedAnnotation) {
        return variable.annAttachments.stream()
                .filter(annotation -> annotation.annotationName.value.equals(expectedAnnotation)).count() > 0;
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
        public BlockingNode blockingNode;
        public SymbolEnv pkgSymbol;
        public BLangInvokableNode invokableNode;
        public DiagnosticPos blockedPos;

        public BlockedNode(SymbolEnv pkgSymbol, BLangInvokableNode invokableNode, DiagnosticPos blockedPos, 
                           BlockingNode blockingNode) {
            this.pkgSymbol = pkgSymbol;
            this.invokableNode = invokableNode;
            this.blockedPos = blockedPos;
            this.blockingNode = blockingNode;
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

            return invokableNode.symbol.pkgID.equals(that.invokableNode.symbol.pkgID) &&
                    invokableNode.symbol.name.equals(that.invokableNode.symbol.name);
        }

        @Override
        public int hashCode() {
            int result = invokableNode.symbol.pkgID.hashCode();
            result = 31 * result + invokableNode.symbol.name.hashCode();
            return result;
        }
    }

    private class TaintError {
        public DiagnosticPos pos;
        public String paramName;

        public TaintError(DiagnosticPos pos, String paramName) {
            this.pos = pos;
            this.paramName = paramName;
        }
    }
}
