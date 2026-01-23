interface PagedContent<T>{
    content: T[],
    empty: boolean,
    first: boolean,
    last: boolean,
    numberOfElements: number,
    totalPages: number,
    pageNumber: number,
    size: number
}