package com.example.scoremaster.domain.dls

class CalculateDlsUseCase(
    private val dlsCalculator: DlsCalculator = DlsCalculator()
) {
    fun execute(input: DlsInput): DlsResult {
        return dlsCalculator.calculate(input)
    }
}
