interface ApiResponse<DataType>{
    success: boolean,
    message?: string,
    data?: DataType,
    error?: string
}