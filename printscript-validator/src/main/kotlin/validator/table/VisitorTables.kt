package validator.table

import common.model.node.BooleanTypeNode
import common.model.node.DivideNode
import common.model.node.MinusNode
import common.model.node.MultiplyNode
import common.model.node.NumberTypeNode
import common.model.node.PlusNode
import common.model.node.StringTypeNode
import common.model.value.operation.AddFloatOperation
import common.model.value.operation.AddIntegerOperation
import common.model.value.operation.AddMixedNumericOperation
import common.model.value.operation.AddStringCoercionOperation
import common.model.value.operation.AddStringOperation
import common.model.value.operation.DivideFloatOperation
import common.model.value.operation.DivideIntegerOperation
import common.model.value.operation.DivideMixedNumericOperation
import common.model.value.operation.MultiplyFloatOperation
import common.model.value.operation.MultiplyIntegerOperation
import common.model.value.operation.MultiplyMixedNumericOperation
import common.model.value.operation.NegateFloatOperation
import common.model.value.operation.NegateIntegerOperation
import common.model.value.operation.PositiveFloatOperation
import common.model.value.operation.PositiveIntegerOperation
import common.model.value.operation.SubtractFloatOperation
import common.model.value.operation.SubtractIntegerOperation
import common.model.value.operation.SubtractMixedNumericOperation
import common.model.value.type.BooleanValueType
import common.model.value.type.NumberValueType
import common.model.value.type.StringValueType
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import validator.visitor.AssignmentVisitor
import validator.visitor.BinaryOperationVisitor
import validator.visitor.BlockVisitor
import validator.visitor.BooleanLiteralVisitor
import validator.visitor.ConstDeclarationVisitor
import validator.visitor.IdentifierVisitor
import validator.visitor.IfStatementVisitor
import validator.visitor.LetDeclarationVisitor
import validator.visitor.NumberLiteralVisitor
import validator.visitor.ParenthesizedExpressionVisitor
import validator.visitor.PrintlnVisitor
import validator.visitor.ReadEnvVisitor
import validator.visitor.ReadInputVisitor
import validator.visitor.StringLiteralVisitor
import validator.visitor.UnaryOperationVisitor

internal object PrintScriptV10 : ContextVisitorTable {
    override val visitors: Collection<ContextVisitor> = listOf(
        AssignmentVisitor(),
        BinaryOperationVisitor(
            mapOf(
                DivideNode to listOf(
                    DivideIntegerOperation,
                    DivideFloatOperation,
                    DivideMixedNumericOperation,
                ),
                MinusNode to listOf(
                    SubtractIntegerOperation,
                    SubtractFloatOperation,
                    SubtractMixedNumericOperation,
                ),
                MultiplyNode to listOf(
                    MultiplyIntegerOperation,
                    MultiplyFloatOperation,
                    MultiplyMixedNumericOperation,
                ),
                PlusNode to listOf(
                    AddIntegerOperation,
                    AddFloatOperation,
                    AddMixedNumericOperation,
                    AddStringOperation,
                    AddStringCoercionOperation,
                ),
            ),
        ),
        IdentifierVisitor(),
        LetDeclarationVisitor(
            mapOf(
                BooleanTypeNode to BooleanValueType,
                NumberTypeNode to NumberValueType,
                StringTypeNode to StringValueType,
            ),
        ),
        NumberLiteralVisitor(),
        ParenthesizedExpressionVisitor(),
        PrintlnVisitor(),
        StringLiteralVisitor(),
        UnaryOperationVisitor(
            mapOf(
                MinusNode to listOf(
                    NegateIntegerOperation,
                    NegateFloatOperation,
                ),
                PlusNode to listOf(
                    PositiveIntegerOperation,
                    PositiveFloatOperation,
                ),
            ),
        ),
    )
}

internal object PrintScriptV11 : ContextVisitorTable {
    override val visitors: Collection<ContextVisitor> = PrintScriptV10.visitors + listOf(
        BlockVisitor(),
        BooleanLiteralVisitor(),
        ConstDeclarationVisitor(
            mapOf(
                BooleanTypeNode to BooleanValueType,
                NumberTypeNode to NumberValueType,
                StringTypeNode to StringValueType,
            ),
        ),
        ReadEnvVisitor(),
        ReadInputVisitor(),
        IfStatementVisitor(),
    )
}
