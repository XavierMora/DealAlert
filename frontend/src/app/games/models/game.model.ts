interface Game{
    id: number,
    steamId: number,
    name: string,
    priceInfo: {
        id: number,
        initialPrice: number,
        finalPrice: number,
        discount: number
    },
    img: string,
    steamUrl: string
}