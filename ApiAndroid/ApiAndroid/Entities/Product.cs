namespace ApiAndroid.Entities
{
    public class Product
    {
        public int ProductId { get; set; }
        public string ProductName { get; set; } = null!;
        public string ProductNumber { get; set; } = null!;
        public virtual List<ProductPhoto> Photos { get; set; } = new List<ProductPhoto>();

        public virtual List<Document> Documents { get; set; } = new List<Document>();
    }
}
