package lexer.scan

import common.model.diagnostic.Diagnostic
import common.model.token.Token

internal sealed interface TokenScan {
    data class Ok(val token: Token) : TokenScan
    data class Error(val diagnostic: Diagnostic) : TokenScan
    data object Empty : TokenScan
}