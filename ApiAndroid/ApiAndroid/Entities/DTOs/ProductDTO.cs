namespace ApiAndroid.Entities.DTOs
{
    public class ProductDTO
    {
        public string ProductName { get; set; }
        public string ProductNumber { get; set; }
        public List<PhotoDto> Photos { get; set; }
        public List<DocumentDto> Documents { get; set; }
    }
}
